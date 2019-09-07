/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSFontFaceRule;
import org.w3c.dom.css.CSSPageRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSUnknownRule;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSNamespaceRule;
import io.sf.carte.doc.style.css.ExtendedCSSStyleSheet;
import io.sf.carte.doc.style.css.MediaQueryList;

/**
 * Abstract class to be inherited by all CSS style sheets.
 * 
 * @author Carlos Amengual
 *
 */
abstract public class AbstractCSSStyleSheet extends AbstractStyleSheet implements ExtendedCSSStyleSheet<AbstractCSSRule> {

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
	abstract public AbstractCSSStyleSheetFactory getStyleSheetFactory();

	/**
	 * Parses a style sheet.
	 * <p>
	 * If the style sheet is not empty, the rules from the parsed source will be
	 * added at the end of the rule list, with the same origin as the rule with
	 * a highest precedence origin.
	 * <p>
	 * The comments preceding a rule will be available through
	 * {@link AbstractCSSRule#getPrecedingComments()}.
	 * <p>
	 * To create a sheet, see
	 * {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#createStyleSheet(String title, io.sf.carte.doc.style.css.MediaQueryList media)
	 * CSSStyleSheetFactory.createStyleSheet(String,MediaQueryList)}
	 * 
	 * @param source
	 *            the SAC input source.
	 * @return <code>true</code> if the SAC parser reported no errors or fatal errors, false
	 *         otherwise.
	 * @throws DOMException
	 *             if a problem is found parsing the sheet.
	 * @throws IOException
	 *             if a problem is found reading the sheet.
	 */
	@Override
	abstract public boolean parseCSSStyleSheet(InputSource source) throws DOMException, IOException;

	/**
	 * Parses a style sheet.
	 * <p>
	 * If the style sheet is not empty, the rules from the parsed source will be
	 * added at the end of the rule list, with the same origin as the rule with
	 * a highest precedence origin.
	 * <p>
	 * If <code>ignoreComments</code> is false, the comments preceding a rule
	 * will be available through {@link AbstractCSSRule#getPrecedingComments()}.
	 * <p>
	 * This method resets the state of this sheet's error handler.
	 * <p>
	 * To create a sheet, see
	 * {@link io.sf.carte.doc.style.css.CSSStyleSheetFactory#createStyleSheet(String title, io.sf.carte.doc.style.css.MediaQueryList media)
	 * CSSStyleSheetFactory.createStyleSheet(String,MediaQueryList)}
	 * 
	 * @param source
	 *            the SAC input source.
	 * @param ignoreComments
	 *            true if comments have to be ignored.
	 * @return <code>true</code> if the SAC parser reported no errors or fatal errors, false
	 *         otherwise.
	 * @throws DOMException
	 *             if a problem is found parsing the sheet.
	 * @throws IOException
	 *             if a problem is found reading the sheet.
	 */
	@Override
	abstract public boolean parseCSSStyleSheet(InputSource source, boolean ignoreComments)
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
	 * @param url
	 *            the url to load the style sheet from.
	 * @param referrerPolicy
	 *            the content of the <code>referrerpolicy</code> content attribute, if any, or
	 *            the empty string.
	 * @return <code>true</code> if the SAC parser reported no errors or fatal errors, <code>false</code> otherwise.
	 * @throws DOMException
	 *             if there is a serious problem parsing the style sheet.
	 * @throws IOException
	 *             if a problem appears fetching the url contents.
	 */
	abstract public boolean loadStyleSheet(URL url, String referrerPolicy) throws DOMException, IOException;

	@Override
	abstract public CSSStyleDeclarationRule createCSSStyleRule();

	@Override
	abstract public CSSFontFaceRule createCSSFontFaceRule();

	@Override
	abstract public ImportRule createCSSImportRule(MediaQueryList mediaList);

	@Override
	abstract public MediaRule createCSSMediaRule(MediaQueryList mediaList);

	@Override
	abstract public CSSPageRule createCSSPageRule();

	abstract public CSSUnknownRule createCSSUnknownRule();

	@Override
	abstract public AbstractCSSStyleDeclaration createCSSStyleDeclaration();

	abstract protected AbstractCSSStyleDeclaration createCSSStyleDeclaration(BaseCSSDeclarationRule rule);

	abstract protected void registerNamespace(CSSNamespaceRule nsrule);

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
			CSSStyleSheet pss = getOwnerRule().getParentStyleSheet();
			if (pss != null) {
				Node node = pss.getOwnerNode();
				if (node != null) {
					doc = (CSSDocument) node.getOwnerDocument();
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
