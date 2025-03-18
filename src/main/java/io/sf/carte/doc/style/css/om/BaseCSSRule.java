/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.LinkedStringList;
import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.CSSBudgetException;
import io.sf.carte.doc.style.css.nsac.CSSErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.CSSNamespaceParseException;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.Parser;

/**
 * Base implementation class for CSS rules.
 * 
 * @author Carlos Amengual
 * 
 */
abstract class BaseCSSRule extends AbstractCSSRule {

	private static final long serialVersionUID = 1L;

	/**
	 * The parent style sheet.
	 */
	private AbstractCSSStyleSheet parentSheet = null;

	/**
	 * The parent rule.
	 */
	private AbstractCSSRule parentRule = null;

	private final short ruleType;

	private final byte ruleOrigin;

	private StringList precedingComments = null;
	private StringList trailingComments = null;

	protected BaseCSSRule(AbstractCSSStyleSheet parentSheet, short type, byte origin) {
		super();
		this.parentSheet = parentSheet;
		ruleType = type;
		ruleOrigin = origin;
	}

	protected BaseCSSRule(BaseCSSRule copyMe) {
		this(copyMe.parentSheet, copyMe.ruleType, copyMe.ruleOrigin);
		this.precedingComments = copyMe.precedingComments;
		this.trailingComments = copyMe.trailingComments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.css.CSSRule#getType()
	 */
	@Override
	public short getType() {
		return ruleType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.css.CSSRule#getParentStyleSheet()
	 */
	@Override
	public AbstractCSSStyleSheet getParentStyleSheet() {
		return parentSheet != null ? parentSheet
				: (parentRule != null) ? parentRule.getParentStyleSheet() : null;
	}

	@Override
	void setParentStyleSheet(AbstractCSSStyleSheet parentSheet) {
		this.parentSheet = parentSheet;
	}

	protected Parser createSACParser() throws DOMException {
		Parser parser;
		if (getParentStyleSheet() != null) {
			parser = getParentStyleSheet().getStyleSheetFactory().createSACParser();
		} else {
			parser = new CSSOMParser();
		}
		return parser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.css.CSSRule#getParentRule()
	 */
	@Override
	public AbstractCSSRule getParentRule() {
		return parentRule;
	}

	/**
	 * Sets the parent CSS rule, in case this rule is contained by another.
	 * 
	 * @param parent
	 *            the parent rule.
	 */
	@Override
	public void setParentRule(AbstractCSSRule parent) {
		parentRule = parent;
	}

	@Override
	public byte getOrigin() {
		return ruleOrigin;
	}

	@Override
	public void enablePrecedingComments() {
		if (precedingComments == null) {
			precedingComments = new LinkedStringList();
		}
	}

	@Override
	public StringList getPrecedingComments() {
		return precedingComments;
	}

	@Override
	void setPrecedingComments(StringList precedingComments) {
		this.precedingComments = precedingComments;
	}

	@Override
	public void enableTrailingComments() {
		if (trailingComments == null) {
			trailingComments = new LinkedStringList();
		}
	}

	@Override
	public StringList getTrailingComments() {
		return trailingComments;
	}

	@Override
	void setTrailingComments(StringList trailingComments) {
		this.trailingComments = trailingComments;
	}

	void resetComments() {
		precedingComments = null;
		trailingComments = null;
	}

	@Override
	boolean hasErrorsOrWarnings() {
		return false;
	}

	@Override
	public String getMinifiedCssText() {
		return getCssText();
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		AbstractCSSStyleSheet parentSS = getParentStyleSheet();
		if (parentSS == null) {
			throw new DOMException(DOMException.INVALID_STATE_ERR,
				"This rule must be added to a sheet first");
		}
		// Create, load & Parse
		BaseCSSStyleSheet css = (BaseCSSStyleSheet) parentSS.getStyleSheetFactory()
			.createRuleStyleSheet(this, null, null);
		CSSHandler handler = css.createSheetHandler(getOrigin(), CSSStyleSheet.COMMENTS_AUTO);
		Reader re = new StringReader(cssText);
		try {
			parseRule(re, handler);
		} catch (IOException e) {
			// This should never happen!
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}

		// Let's see how many rules we got
		CSSRuleArrayList parsedRules = css.getCssRules();
		int len = parsedRules.getLength();
		if (len > 1) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
				"Attempted to parse more than one rule inside this one");
		}
		if (len == 1) {
			AbstractCSSRule firstRule = parsedRules.item(0);
			if (firstRule.getType() != getType()) {
				throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"Attempted to parse a rule of type " + firstRule.getType());
			}
			setRule(firstRule);
			if (css.hasRuleErrorsOrWarnings()) {
				parentSS.getErrorHandler().mergeState(css.getErrorHandler());
			}
		} else {
			// Clear the rule (in some rules this has no effect).
			clear();
		}
	}

	abstract void clear();

	void parseRule(Reader reader, CSSHandler handler) throws IOException {
		// Create and configure a parser
		Parser parser = createSACParser();
		// Allow only warnings
		CSSErrorHandler errorHandler = new AllowWarningsRuleErrorHandler();
		parser.setDocumentHandler(handler);
		parser.setErrorHandler(errorHandler);

		// Parse
		parseRule(reader, parser);
	}

	void parseRule(Reader reader, Parser parser)
			throws DOMException, IOException {
		try {
			parser.parseRule(reader);
		} catch (CSSNamespaceParseException e) {
			DOMException ex = new DOMException(DOMException.NAMESPACE_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSBudgetException e) {
			DOMException ex = new DOMException(DOMException.NOT_SUPPORTED_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSParseException e) {
			DOMException ex = new DOMException(DOMException.SYNTAX_ERR, "Parse error at ["
				+ e.getLineNumber() + ',' + e.getColumnNumber() + "]: " + e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (CSSException e) {
			DOMException ex = new DOMException(DOMException.INVALID_ACCESS_ERR, e.getMessage());
			ex.initCause(e);
			throw ex;
		} catch (DOMException e) {
			// Handler may produce DOM exceptions
			throw e;
		} catch (RuntimeException e) {
			String message = e.getMessage();
			AbstractCSSStyleSheet parentSS = getParentStyleSheet();
			if (parentSS != null) {
				String href = parentSS.getHref();
				if (href != null) {
					message = "Error in stylesheet at " + href + ": " + message;
				}
			}
			DOMException ex = new DOMException(DOMException.INVALID_STATE_ERR, message);
			ex.initCause(e);
			throw ex;
		}
	}

	abstract void setRule(AbstractCSSRule copyMe);

	protected StyleFormattingContext getStyleFormattingContext() {
		StyleFormattingContext context;
		AbstractCSSStyleSheet sheet = getParentStyleSheet();
		if (sheet != null) {
			context = sheet.getStyleSheetFactory().getStyleFormattingFactory().createStyleFormattingContext();
		} else {
			context = new DefaultStyleFormattingContext();
		}
		return context;
	}

	/**
	 * Gets an URL for the given URI, taking into account the parent CSS Base URL if
	 * appropriate.
	 * 
	 * @param uri the uri.
	 * @return the absolute URL.
	 * @throws MalformedURLException if the URL could not be built.
	 */
	protected URL getURL(String uri) throws MalformedURLException {
		if (uri == null || uri.isEmpty()) {
			throw new MalformedURLException("Empty URI");
		}
		URI destUri;
		try {
			destUri = new URI(uri);
		} catch (URISyntaxException e) {
			throw new MalformedURLException(e.getMessage());
		}
		String phref = getParentStyleSheet().getHref();
		if (!destUri.isAbsolute()) {
			if (phref == null) {
				throw new MalformedURLException("Cannot convert to absolute URI " + uri);
			}
			URI pUri;
			try {
				pUri = new URI(phref);
				destUri = pUri.resolve(destUri);
			} catch (Exception e) {
				throw new MalformedURLException(e.getMessage());
			}
		}
		return destUri.toURL();
	}

	/**
	 * Error handler that allows warnings but no exceptions.
	 */
	class AllowWarningsRuleErrorHandler implements CSSErrorHandler {

		@Override
		public void warning(CSSParseException exception) throws CSSParseException {
			AbstractCSSStyleSheet sheet = getParentStyleSheet();
			if (sheet != null) {
				sheet.getErrorHandler().ruleParseWarning(BaseCSSRule.this, exception);
			}
		}

		@Override
		public void error(CSSParseException exception) throws CSSParseException {
			throw exception;
		}

	}

}
