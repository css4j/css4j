/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.net.URL;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import io.sf.carte.doc.DOMPolicyException;
import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSImportRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSImportRule.
 */
public class ImportRule extends BaseCSSRule implements CSSImportRule, CSSRule {

	private static final long serialVersionUID = 1L;

	private static final int MAX_IMPORT_RECURSION = 8; // Allows 7 nested imports

	private AbstractCSSStyleSheet importedSheet = null;

	private String styleSheetURI;

	private String layerName;

	private BooleanCondition supportsCondition;

	private MediaQueryList mediaList;

	/**
	 * Construct an import rule with the given parameters.
	 * 
	 * @param parentSheet       the parent style sheet.
	 * @param layerName         the layer name declared in the at-rule itself, or an
	 *                          empty string if the layer is anonymous, or
	 *                          {@code null} if the at-rule does not declare a
	 *                          layer.
	 * @param supportsCondition the supports condition, or {@code null} if none.
	 * @param mediaList         the media list to which the sheet shall apply.
	 * @param href              the URI from which to import the sheet.
	 * @param origin            the origin of the rule.
	 */
	protected ImportRule(AbstractCSSStyleSheet parentSheet, String layerName,
			BooleanCondition supportsCondition, MediaQueryList mediaList, String href,
			int origin) {
		super(parentSheet, CSSRule.IMPORT_RULE, origin);
		this.layerName = layerName;
		this.supportsCondition = supportsCondition;
		this.mediaList = mediaList;
		this.styleSheetURI = href;
	}

	@Override
	public String getHref() {
		return styleSheetURI;
	}

	@Override
	public String getLayerName() {
		return layerName;
	}

	@Override
	public BooleanCondition getSupportsCondition() {
		return supportsCondition;
	}

	@Override
	public MediaQueryList getMedia() {
		return mediaList;
	}

	@Override
	public AbstractCSSStyleSheet getStyleSheet() {
		if (importedSheet == null) {
			AbstractCSSStyleSheet parent = getParentStyleSheet();
			importedSheet = parent.getStyleSheetFactory().createRuleStyleSheet(this,
					parent.getTitle(), mediaList);
			importedSheet.setParentStyleSheet(parent);
			// Load the sheet
			try {
				loadStyleSheet();
			} catch (DOMPolicyException e) {
				// Already logged
				importedSheet = null;
			} catch (DOMException e) {
				// The exception was already reported, but here we give the rule text.
				parent.getErrorHandler().badAtRule(e, getCssText());
			} catch (IOException e) {
				parent.getDocumentErrorHandler().ioError(styleSheetURI, e);
			}
		}
		return importedSheet;
	}

	/**
	 * Loads and parses an imported CSS style sheet.
	 * 
	 * @return <code>true</code> if the NSAC parser reported no errors or fatal
	 *         errors and the sheet URL is allowed, <code>false</code> otherwise.
	 * @throws IOException  if a problem appears fetching or resolving the uri
	 *                      contents.
	 * @throws DOMException if there is a problem building the sheet's DOM.
	 */
	private boolean loadStyleSheet() throws IOException, DOMException {
		URL styleSheetURL = getURL(getHref());
		Node owner = getParentStyleSheet().getOwnerNode();
		CSSDocument cssdoc;
		if (owner != null) {
			if (owner.getNodeType() == Node.DOCUMENT_NODE) {
				cssdoc = (CSSDocument) owner;
			} else {
				cssdoc = (CSSDocument) owner.getOwnerDocument();
			}
		} else {
			cssdoc = null;
		}
		if (cssdoc != null && !cssdoc.isAuthorizedOrigin(styleSheetURL)) {
			cssdoc.getErrorHandler().policyError(owner,
					"Unauthorized @import URL: " + styleSheetURL.toExternalForm());
			return false;
		}
		// load & Parse
		return importedSheet.loadStyleSheet(styleSheetURL, "");
	}

	@Override
	int addToSheet(AbstractCSSStyleSheet sheet, int importCount) {
		if (layerName != null) {
			// No layer support yet
			return importCount;
		}

		importCount++;
		if (importCount >= MAX_IMPORT_RECURSION) {
			handleTooManyNested(sheet);
			return importCount;
		}

		// We clone with same parent, to receive the errors
		ImportRule imp = (ImportRule) clone();
		AbstractCSSStyleSheet impSheet = imp.getStyleSheet();
		CSSRuleArrayList impRules = impSheet.getCssRules();
		MediaQueryList media = imp.getMedia();
		if (media.isAllMedia()) {
			importCount = sheet.addRuleList(impRules, importCount);
		} else if (!media.isNotAllMedia()) {
			// Add as a Media rule
			MediaRule mrule = sheet.createMediaRule(media);
			importCount = mrule.addRuleList(impRules, importCount);
			sheet.addLocalRule(mrule);
		}

		return importCount;
	}

	private void handleTooManyNested(AbstractCSSStyleSheet sheet) {
		DOMException ex = new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
				"Too many nested imports");
		String cssText = getCssText();
		if (sheet != null) {
			sheet.getErrorHandler().badAtRule(ex, cssText);
		}
		getParentStyleSheet().getErrorHandler().badAtRule(ex, cssText);
	}

	@Override
	int addToMediaRule(MediaRule mrule, int importCount) {
		importCount++;
		if (importCount == MAX_IMPORT_RECURSION) {
			handleTooManyNested(null);
			return importCount;
		}
		// We clone with same parent, to receive the errors
		ImportRule imp = (ImportRule) clone();
		AbstractCSSStyleSheet impSheet = imp.getStyleSheet();
		CSSRuleArrayList impRules = impSheet.getCssRules();
		MediaQueryList media = imp.getMedia();
		if (mrule.getMedia().equals(media)) {
			// Add to the same media rule
			importCount = mrule.addRuleList(impRules, importCount);
		} else {
			// Add as a new media rule
			AbstractCSSStyleSheet parent = getParentStyleSheet();
			MediaRule mrule2 = parent.createMediaRule(mediaList);
			mrule.addRule(mrule2);
			importCount = mrule2.addRuleList(impRules, importCount);
		}
		return importCount;
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
		if (layerName != null) {
			wri.write(' ');
			if (layerName.isEmpty()) {
				wri.write('"');
				wri.write('"');
			} else {
				wri.write("layer(");
				wri.write(layerName);
				wri.write(')');
			}
		}
		if (supportsCondition != null) {
			wri.write(" supports(");
			wri.write(supportsCondition.toString());
			wri.write(')');
		}
		if (!mediaList.isAllMedia()) {
			wri.write(' ');
			wri.write(mediaList.getMedia());
		}
		context.writeSemiColon(wri);
	}

	@Override
	public String getMinifiedCssText() {
		StringBuilder buf = new StringBuilder(80);
		buf.append("@import ");
		buf.append(ParseHelper.quote(styleSheetURI, '\''));
		if (layerName != null) {
			buf.append(' ');
			if (layerName.isEmpty()) {
				buf.append('"');
				buf.append('"');
			} else {
				buf.append("layer(");
				buf.append(layerName);
				buf.append(')');
			}
		}
		if (supportsCondition != null) {
			buf.append(" supports(");
			supportsCondition.appendMinifiedText(buf);
			buf.append(')');
		}
		if (!mediaList.isAllMedia()) {
			buf.append(' ');
			buf.append(mediaList.getMinifiedMedia());
		}
		buf.append(';');
		return buf.toString();
	}

	@Override
	boolean hasErrorsOrWarnings() {
		return mediaList != null && mediaList.hasErrors();
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
		ImportRule rule = new ImportRule(parentSheet, layerName, supportsCondition,
				((MediaListAccess) getMedia()).unmodifiable(), getHref(), getOrigin());
		return rule;
	}

}
