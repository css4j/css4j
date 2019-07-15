/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.net.URL;

import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.SACMediaList;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSImportRule.
 * 
 * @author Carlos Amengual
 * 
 */
public class ImportRule extends BaseCSSRule implements CSSImportRule, ExtendedCSSRule {

	private AbstractCSSStyleSheet loadedSheet = null;

	private String styleSheetURI = null;

	private MediaQueryList mediaList;

	public ImportRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.IMPORT_RULE, origin);
		mediaList = MediaList.createMediaList();
	}

	ImportRule(AbstractCSSStyleSheet parentSheet, MediaQueryList mediaList, byte origin) {
		super(parentSheet, CSSRule.IMPORT_RULE, origin);
		this.mediaList = mediaList;
	}

	@Override
	public String getHref() {
		return styleSheetURI;
	}

	@Override
	public MediaQueryList getMedia() {
		return mediaList;
	}

	@Override
	public AbstractCSSStyleSheet getStyleSheet() {
		return loadedSheet;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCssText() {
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(230);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		wri.write("@import ");
		context.writeURL(wri, getStyleSheet().getHref());
		if (!mediaList.isAllMedia()) {
			wri.write(' ');
			wri.write(mediaList.getMediaText());
		}
		context.writeSemiColon(wri);
	}

	/**
	 * Loads and parses an imported CSS style sheet.
	 * 
	 * @param uri
	 *            the URI to import the sheet.
	 * @param title
	 *            the advisory title of the imported sheet. If not set, will try
	 *            to get the title from the parent style sheet.
	 * @param media
	 *            the destination SAC media list for the style information.
	 * @return <code>true</code> if the SAC parser reported no errors or fatal errors, false
	 *         otherwise.
	 * @throws CSSException
	 *             if there is a serious problem parsing the style sheet at low
	 *             level.
	 * @throws IOException
	 *             if a problem appears fetching or resolving the uri contents.
	 * @throws DOMException
	 *             if there is a problem building the sheet's DOM.
	 */
	public boolean loadStyleSheet(String uri, String title, SACMediaList media)
			throws CSSException, IOException, DOMException {
		URL styleSheetURL = getURL(uri);
		styleSheetURI = styleSheetURL.toExternalForm();
		((MediaListAccess) mediaList).appendSACMediaList(media);
		AbstractCSSStyleSheet parentSS = getParentStyleSheet();
		if (title == null) {
			if (parentSS != null) {
				title = parentSS.getTitle();
			}
		}
		// Create, load & Parse
		AbstractCSSStyleSheet css = parentSS.getStyleSheetFactory().createRuleStyleSheet(this, title,
				mediaList);
		css.setParentStyleSheet(parentSS);
		loadedSheet = css;
		return css.loadStyleSheet(styleSheetURL, "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mediaList == null) ? 0 : mediaList.hashCode());
		result = prime * result + ((styleSheetURI == null) ? 0 : styleSheetURI.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ImportRule other = (ImportRule) obj;
		if (mediaList == null) {
			if (other.mediaList != null) {
				return false;
			}
		} else if (!mediaList.equals(other.mediaList)) {
			return false;
		}
		if (styleSheetURI == null) {
			if (other.styleSheetURI != null) {
				return false;
			}
		} else if (!styleSheetURI.equals(other.styleSheetURI)) {
			return false;
		}
		return true;
	}

	@Override
	public ImportRule clone(AbstractCSSStyleSheet parentSheet) {
		ImportRule rule = new ImportRule(parentSheet, ((MediaListAccess) getMedia()).unmodifiable(), getOrigin());
		rule.styleSheetURI = getHref();
		rule.loadedSheet = getStyleSheet();
		return rule;
	}

}
