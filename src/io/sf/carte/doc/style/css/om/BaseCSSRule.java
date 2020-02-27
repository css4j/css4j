/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.Parser;

/**
 * Base implementation class for CSS rules.
 * 
 * @author Carlos Amengual
 * 
 */
abstract class BaseCSSRule extends AbstractCSSRule {

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

	List<String> precedingComments = null;
	List<String> trailingComments = null;

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
		return parentSheet;
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
	public void enablePrecedingComments(int initialSize) {
		if (precedingComments == null) {
			precedingComments = new ArrayList<String>(initialSize);
		}
	}

	@Override
	public List<String> getPrecedingComments() {
		return precedingComments;
	}

	@Override
	void setPrecedingComments(List<String> precedingComments) {
		this.precedingComments = precedingComments;
	}

	@Override
	public List<String> getTrailingComments() {
		return trailingComments;
	}

	@Override
	public String getMinifiedCssText() {
		return getCssText();
	}

	void resetComments() {
		precedingComments = null;
		trailingComments = null;
	}

	@Override
	boolean hasErrorsOrWarnings() {
		return false;
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
	 * Gets an URL for the given URI, taking into account the parent CSS Base
	 * URL if appropriate.
	 * 
	 * @param uri
	 *            the uri.
	 * @return the absolute URL.
	 * @throws MalformedURLException
	 *             if the uri was wrong.
	 */
	protected URL getURL(String uri) throws MalformedURLException {
		if (uri.length() == 0) {
			throw new MalformedURLException("Empty URI");
		}
		URL url;
		if (uri.indexOf("://") < 0) {
			String phref = getParentStyleSheet().getHref();
			if (phref == null) {
				throw new MalformedURLException("Relative URI but no href for parent style sheet.");
			}
			URL pUrl = new URL(phref);
			url = new URL(pUrl, uri);
		} else {
			url = new URL(uri);
		}
		return url;
	}

}
