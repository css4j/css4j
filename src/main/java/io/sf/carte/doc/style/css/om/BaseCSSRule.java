/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.LinkedStringList;
import io.sf.carte.doc.StringList;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.Parser;

/**
 * Base implementation class for CSS rules.
 */
abstract class BaseCSSRule extends AbstractCSSRule {

	private static final long serialVersionUID = 2L;

	/**
	 * The parent style sheet.
	 */
	private AbstractCSSStyleSheet parentSheet = null;

	/**
	 * The parent rule.
	 */
	private AbstractCSSRule parentRule = null;

	private final short ruleType;

	private final int ruleOrigin;

	private StringList precedingComments = null;
	private StringList trailingComments = null;

	protected BaseCSSRule(AbstractCSSStyleSheet parentSheet, short type, int origin) {
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
	public int getOrigin() {
		return ruleOrigin;
	}

	@Override
	int addToSheet(AbstractCSSStyleSheet sheet, int importCount) {
		AbstractCSSRule clone = clone();
		sheet.addLocalRule(clone);
		return importCount;
	}

	@Override
	int addToMediaRule(MediaRule mrule, int importCount) {
		AbstractCSSRule clon = clone();
		mrule.addRule(clon);
		return importCount;
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

}
