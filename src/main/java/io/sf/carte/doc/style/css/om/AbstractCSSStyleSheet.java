/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.DOMPolicyException;
import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSDeclarationRule;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSNamespaceRule;
import io.sf.carte.doc.style.css.CSSStyleRule;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.util.Visitor;

/**
 * Abstract class to be inherited by all CSS style sheets.
 * 
 * @author Carlos Amengual
 *
 */
abstract public class AbstractCSSStyleSheet extends AbstractStyleSheet implements CSSStyleSheet<AbstractCSSRule> {

	private static final long serialVersionUID = 1L;

	static final int CONNECT_TIMEOUT = 10000;

	/*
	 * The title is in the constructor because it being intern is part of the
	 * AbstractCSSStyleSheet contract.
	 */
	protected AbstractCSSStyleSheet(String title) {
		super(title);
	}

	@Override
	abstract public CSSRuleArrayList getCssRules();

	@Override
	abstract public AbstractCSSStyleSheet getParentStyleSheet();

	@Override
	abstract public AbstractCSSRule getOwnerRule();

	@Override
	abstract public AbstractCSSStyleSheetFactory getStyleSheetFactory();

	/**
	 * Parses a style sheet.
	 * <p>
	 * If the style sheet is not empty, the rules from the parsed source will be
	 * added at the end of the rule list, with the same origin as the rule with
	 * a highest precedence origin.
	 * <p>
	 * Even if a specific media is set at the <code>InputSource</code>, this method
	 * does not alter the sheet's current media attribute.
	 * <p>
	 * The comments shall be processed according to {@link CSSStyleSheet#COMMENTS_AUTO}.
	 * <p>
	 * To create a sheet, see
	 * {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#createStyleSheet(String title, io.sf.carte.doc.style.css.MediaQueryList media)
	 * CSSStyleSheetFactory.createStyleSheet(String,MediaQueryList)}
	 * 
	 * @param reader
	 *            the character stream containing the CSS sheet.
	 * @return <code>true</code> if the NSAC parser reported no errors or fatal errors, false
	 *         otherwise.
	 * @throws DOMException
	 *             if a problem is found parsing the sheet.
	 * @throws IOException
	 *             if a problem is found reading the sheet.
	 */
	@Override
	abstract public boolean parseStyleSheet(Reader reader) throws DOMException, IOException;

	/**
	 * Parses a style sheet.
	 * <p>
	 * If the style sheet is not empty, the rules from the parsed source will be
	 * added at the end of the rule list, with the same origin as the rule with a
	 * highest precedence origin.
	 * <p>
	 * Even if a specific media is set at the <code>InputSource</code>, this method
	 * does not alter the sheet's current media attribute.
	 * <p>
	 * If <code>commentMode</code> is not {@code COMMENTS_IGNORE}, the comments
	 * preceding a rule shall be available through
	 * {@link AbstractCSSRule#getPrecedingComments()}, and if {@code COMMENTS_AUTO}
	 * was set also the trailing ones, through the method
	 * {@link AbstractCSSRule#getTrailingComments()}.
	 * <p>
	 * This method resets the state of this sheet's error handler.
	 * <p>
	 * To create a sheet, see
	 * {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#createStyleSheet(String title, io.sf.carte.doc.style.css.MediaQueryList media)
	 * CSSStyleSheetFactory.createStyleSheet(String,MediaQueryList)}
	 * 
	 * @param reader      the character stream containing the CSS sheet.
	 * @param commentMode {@code 0} if comments have to be ignored, {@code 1} if all
	 *                    comments are considered as preceding a rule, {@code 2} if
	 *                    the parser should try to figure out which comments are
	 *                    preceding and trailing a rule (auto mode).
	 * @return <code>true</code> if the NSAC parser reported no errors or fatal
	 *         errors, false otherwise.
	 * @throws DOMException if a problem is found parsing the sheet.
	 * @throws IOException  if a problem is found reading the sheet.
	 */
	@Override
	abstract public boolean parseStyleSheet(Reader reader, short commentMode)
			throws DOMException, IOException;

	/**
	 * Adds the rules contained by the supplied style sheet, if that sheet is
	 * not disabled.
	 * <p>
	 * If the provided sheet does not target all media, a media rule is created.
	 * 
	 * @param sheet
	 *            the sheet whose rules are to be added.
	 */
	@Override
	abstract public void addStyleSheet(AbstractCSSStyleSheet sheet);

	/**
	 * Load the styles from <code>url</code> into this style sheet.
	 * 
	 * @param url            the url to load the style sheet from.
	 * @param referrerPolicy the content of the <code>referrerpolicy</code> content
	 *                       attribute, if any, or the empty string.
	 * @return <code>true</code> if the NSAC parser reported no errors or fatal
	 *         errors, <code>false</code> otherwise.
	 * @throws DOMPolicyException if the style sheet was served with an invalid
	 *                            content type.
	 * @throws DOMException       if there is a serious problem parsing the style
	 *                            sheet.
	 * @throws IOException        if a problem appears fetching the url contents.
	 */
	abstract public boolean loadStyleSheet(URL url, String referrerPolicy) throws DOMException, IOException;

	@Override
	abstract public FontFaceRule createFontFaceRule();

	@Override
	abstract public ImportRule createImportRule(MediaQueryList mediaList, String href);

	@Override
	abstract public MediaRule createMediaRule(MediaQueryList mediaList);

	@Override
	abstract public PageRule createPageRule();

	@Override
	abstract public StyleRule createStyleRule();

	@Override
	abstract public SupportsRule createSupportsRule(String conditionText) throws DOMException;

	@Override
	abstract public SupportsRule createSupportsRule(BooleanCondition condition);

	@Override
	@Deprecated
	abstract public SupportsRule createSupportsRule();

	@Override
	abstract public UnknownRule createUnknownRule();

	@Override
	abstract public ViewportRule createViewportRule();

	@Override
	abstract public AbstractCSSStyleDeclaration createStyleDeclaration();

	/**
	 * Create a style declaration from the given declaration rule.
	 * 
	 * @param rule the declaration rule.
	 * @return the style declaration.
	 */
	abstract protected AbstractCSSStyleDeclaration createStyleDeclaration(
		BaseCSSDeclarationRule rule);

	/**
	 * Register the namespace from the given namespace rule.
	 * 
	 * @param nsrule the namespace rule.
	 */
	abstract protected void registerNamespace(CSSNamespaceRule nsrule);

	/**
	 * Unregister the namespace corresponding to the given namespace URI.
	 * 
	 * @param namespaceURI the namespace URI.
	 */
	abstract protected void unregisterNamespace(String namespaceURI);

	/**
	 * Gets the namespace prefix associated to the given URI.
	 * 
	 * @param uri
	 *            the namespace URI string.
	 * @return the namespace prefix.
	 */
	abstract protected String getNamespacePrefix(String uri);

	/**
	 * Has this style sheet defined a default namespace ?
	 * 
	 * @return <code>true</code> if a default namespace was defined, <code>false</code> otherwise.
	 */
	abstract protected boolean hasDefaultNamespace();

	abstract public void setHref(String href);

	/**
	 * Get the origin of this sheet.
	 * 
	 * @return the origin of this sheet.
	 */
	abstract public byte getOrigin();

	/**
	 * Clone this style sheet.
	 * 
	 * @return the cloned style sheet.
	 */
	@Override
	abstract public AbstractCSSStyleSheet clone();

	@Override
	public URLConnection openConnection(URL url, String referrerPolicy) throws IOException {
		// Try to get the parent document to handle the connection
		CSSDocument doc = null;
		if (getOwnerNode() != null) {
			doc = (CSSDocument) getOwnerNode().getOwnerDocument();
		} else if (getOwnerRule() != null) {
			AbstractCSSStyleSheet pss = getOwnerRule().getParentStyleSheet();
			if (pss != null) {
				Node node = pss.getOwnerNode();
				if (node != null) {
					if (node.getNodeType() != Node.DOCUMENT_NODE) {
						doc = (CSSDocument) node.getOwnerDocument();
					} else {
						doc = (CSSDocument) node;
					}
				}
			}
		}
		// Connect
		URLConnection ucon;
		if (doc != null) {
			ucon = doc.openConnection(url);
			String docuri = doc.getDocumentURI();
			if (docuri != null) {
				if ("".equals(referrerPolicy)) {
					referrerPolicy = doc.getReferrerPolicy();
				}
				String referrer = getReferrer(docuri, url, referrerPolicy);
				if (referrer != null) {
					ucon.setRequestProperty("Referer", referrer);
				}
			}
		} else {
			ucon = url.openConnection();
		}
		ucon.setAllowUserInteraction(false);
		ucon.setConnectTimeout(CONNECT_TIMEOUT);
		return ucon;
	}

	private String getReferrer(String referrer, URL destinationUrl, String referrerPolicy) {
		if ("no-referrer".equals(referrerPolicy)) {
			referrer = null;
		} else if ("origin".equals(referrerPolicy)) {
			referrer = getOrigin(referrer);
		} else if ("same-origin".equals(referrerPolicy)) {
			referrer = getSameOrigin(referrer, destinationUrl);
		} else if ("strict-origin".equals(referrerPolicy)) {
			referrer = getStrictOrigin(referrer, destinationUrl);
		} else if (!"unsafe-url".equals(referrerPolicy)) {
			// "no-referrer-when-downgrade" is the default
			referrer = getNoReferrerWhenDowngrade(referrer, destinationUrl);
		}
		return referrer;
	}

	private String getOrigin(String referrer) {
		URL url;
		try {
			url = new URL(referrer);
			if (!isLocalScheme(url.getProtocol())) {
				URL refUrl = new URL(url.getProtocol(), url.getHost(), urlPort(url), "/");
				return refUrl.toExternalForm();
			}
		} catch (MalformedURLException e) {
		}
		return null;
	}

	private String getSameOrigin(String referrer, URL destinationUrl) {
		URL url;
		try {
			url = new URL(referrer);
			if (!isLocalScheme(url.getProtocol())) {
				String desthost = destinationUrl.getHost();
				if (desthost != null && desthost.equalsIgnoreCase(url.getHost())) {
					URL refUrl = new URL(url.getProtocol(), url.getHost(), urlPort(url), url.getFile());
					return refUrl.toExternalForm();
				}
			}
		} catch (MalformedURLException e) {
		}
		return null;
	}

	/* @formatter:off
	 * 
	 * Only return referrer for requests:
	 * - From a TLS-protected environment settings object to a potentially trustworthy URL.
	 * - From non-TLS-protected environment settings objects to any origin.
	 * 
	 * @formatter:on
	 */
	private String getStrictOrigin(String referrer, URL destinationUrl) {
		URL url;
		try {
			url = new URL(referrer);
			if (!isLocalScheme(url.getProtocol())) {
				String destproto = destinationUrl.getProtocol();
				String desthost = destinationUrl.getHost();
				if (!"https".equals(url.getProtocol()) || ("https".equals(destproto) && desthost != null
						&& desthost.equalsIgnoreCase(url.getHost()))) {
					URL refUrl = new URL(url.getProtocol(), url.getHost(), urlPort(url), "/");
					return refUrl.toExternalForm();
				}
			}
		} catch (MalformedURLException e) {
		}
		return null;
	}

	private String getNoReferrerWhenDowngrade(String referrer, URL destinationUrl) {
		URL url;
		try {
			url = new URL(referrer);
			if (!isLocalScheme(url.getProtocol())) {
				String destproto = destinationUrl.getProtocol();
				if (!"https".equals(url.getProtocol()) || "https".equals(destproto)) {
					URL refUrl = new URL(url.getProtocol(), url.getHost(), urlPort(url), url.getFile());
					return refUrl.toExternalForm();
				}
			}
		} catch (MalformedURLException e) {
		}
		return null;
	}

	private boolean isLocalScheme(String proto) {
		return !"https".equals(proto) && !"http".equals(proto);
	}

	private int urlPort(URL url) {
		int port = url.getPort();
		if (port == url.getDefaultPort()) {
			port = -1;
		}
		return port;
	}

	/**
	 * Get the first style rule that exactly matches the given selector list, if
	 * any.
	 * <p>
	 * Rules inside grouping rules are also searched.
	 * </p>
	 * 
	 * @param selectorList the selector list.
	 * @return the first style rule that matches, or {@code null} if none.
	 */
	abstract public StyleRule getFirstStyleRule(SelectorList selectorList);

	/**
	 * Get the list of style rules that match the given selector.
	 * <p>
	 * Rules inside grouping rules are also searched.
	 * </p>
	 * 
	 * @param selector the selector.
	 * @return the list of style rule that match, or {@code null} if none.
	 */
	abstract public CSSRuleArrayList getStyleRules(Selector selector);

	/**
	 * Accept a style rule visitor.
	 * 
	 * @param visitor the visitor.
	 */
	abstract public void acceptStyleRuleVisitor(Visitor<CSSStyleRule> visitor);

	/**
	 * Accept a declaration rule visitor.
	 * 
	 * @param visitor the visitor.
	 */
	abstract public void acceptDeclarationRuleVisitor(Visitor<CSSDeclarationRule> visitor);

	/**
	 * Accept a descriptor rule visitor.
	 * <p>
	 * This method scans for declaration rules that declare descriptors.
	 * </p>
	 * 
	 * @param visitor the visitor.
	 */
	abstract public void acceptDescriptorRuleVisitor(Visitor<CSSDeclarationRule> visitor);

	/**
	 * Sets the parent style sheet.
	 * 
	 * @param parent the parent style sheet. Cannot be {@code null}.
	 */
	abstract protected void setParentStyleSheet(AbstractCSSStyleSheet parent);

	/**
	 * Parses the <code>rel</code> attribute.
	 * 
	 * @param relText
	 *            the <code>rel</code> attribute.
	 * @return 0 if <code>rel</code> refers to a style sheet, 1 if the sheet is alternate, -1
	 *         if it is not compatible with a style sheet.
	 */
	public static byte parseRelAttribute(String relText) {
		boolean styleSheet = false;
		boolean alternate = false;
		byte result = -1;
		relText = relText.trim();
		int idx = relText.indexOf(' ');
		if (idx == -1) {
			styleSheet = relText.length() == 0 || relText.equalsIgnoreCase("stylesheet");
			if (!styleSheet) {
				alternate = "alternate".equalsIgnoreCase(relText);
				styleSheet = alternate;
			}
		} else {
			String first = relText.substring(0, idx);
			String second = relText.substring(idx + 1).trim();
			alternate = "alternate".equalsIgnoreCase(first) || "alternate".equalsIgnoreCase(second);
			styleSheet = alternate && ("stylesheet".equalsIgnoreCase(second) || "stylesheet".equalsIgnoreCase(first));
		}
		if (styleSheet) {
			result = alternate ? (byte) 1 : 0;
		}
		return result;
	}

}
