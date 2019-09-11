/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.net.URL;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.ExtendedCSSRule;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSImportRule.
 * 
 * @author Carlos Amengual
 * 
 */
public class ImportRule extends BaseCSSRule implements CSSImportRule, ExtendedCSSRule {

	private AbstractCSSStyleSheet importedSheet = null;

	private final String styleSheetURI;

	private final MediaQueryList mediaList;

	/**
	 * Construct an import rule with the given parameters.
	 * 
	 * @param parentSheet the parent style sheet.
	 * @param mediaList   the media list to which the sheet shall apply.
	 * @param href        the URI from which to import the sheet.
	 * @param origin      the origin of the rule.
	 */
	ImportRule(AbstractCSSStyleSheet parentSheet, MediaQueryList mediaList, String href, byte origin) {
		super(parentSheet, CSSRule.IMPORT_RULE, origin);
		this.mediaList = mediaList;
		this.styleSheetURI = href;
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
		if (importedSheet == null) {
			AbstractCSSStyleSheet parent = getParentStyleSheet();
			importedSheet = parent.getStyleSheetFactory().createRuleStyleSheet(this, parent.getTitle(),
					mediaList);
			importedSheet.setParentStyleSheet(parent);
			// Load the sheet
			try {
				loadStyleSheet();
			} catch (DOMException e) {
				parent.getErrorHandler().badAtRule(e, getCssText());
			} catch (IOException e) {
				parent.getErrorHandler().ruleIOError(this, e);
			}
		}
		return importedSheet;
	}

	/**
	 * Loads and parses an imported CSS style sheet.
	 * 
	 * @return <code>true</code> if the SAC parser reported no errors or fatal errors, false
	 *         otherwise.
	 * @throws IOException
	 *             if a problem appears fetching or resolving the uri contents.
	 * @throws DOMException
	 *             if there is a problem building the sheet's DOM.
	 */
	private boolean loadStyleSheet() throws IOException, DOMException {
		URL styleSheetURL = getURL(getHref());
		// load & Parse
		return importedSheet.loadStyleSheet(styleSheetURL, "");
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "@import rules must be created from a style sheet.");
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
		context.writeURL(wri, getHref());
		if (!mediaList.isAllMedia()) {
			wri.write(' ');
			wri.write(mediaList.getMediaText());
		}
		context.writeSemiColon(wri);
	}

	@Override
	public String getMinifiedCssText() {
		StringBuilder buf = new StringBuilder(80);
		buf.append("@import ");
		buf.append(ParseHelper.quote(styleSheetURI, '\''));
		if (!mediaList.isAllMedia()) {
			buf.append(' ');
			buf.append(mediaList.getMinifiedMedia());
		}
		buf.append(';');
		return buf.toString();
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
		ImportRule rule = new ImportRule(parentSheet, ((MediaListAccess) getMedia()).unmodifiable(), getHref(),
				getOrigin());
		return rule;
	}

}
