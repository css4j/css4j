/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.om.AbstractCSSStyleSheet;

class StyleDefinerElementHelper {

	private final DOMElement element;

	private AbstractCSSStyleSheet definedSheet = null;

	private boolean needsUpdate = true;

	StyleDefinerElementHelper(DOMElement element) {
		super();
		this.element = element;
	}

	AbstractCSSStyleSheet getInlineSheet() {
		if (needsUpdate) {
			String type = element.getAttribute("type");
			if (!"text/css".equalsIgnoreCase(type) && type.length() != 0) {
				return null;
			}
			MediaQueryList mediaList = element.getOwnerDocument().parseMediaList(element.getAttribute("media").trim(),
					element);
			if (mediaList == null) {
				return null;
			}
			String title = element.getAttribute("title").trim();
			if (title.length() == 0) {
				title = null;
			}
			String styleText = element.getTextContent().trim();
			definedSheet = element.getOwnerDocument().parseEmbeddedStyleSheet(definedSheet, styleText, title, mediaList,
					element);
			needsUpdate = false;
		}
		return definedSheet;
	}

	AbstractCSSStyleSheet getLinkedSheet() {
		if (needsUpdate) {
			String rel = element.getAttribute("rel");
			String type = element.getAttribute("type");
			int typelen = type.length();
			if (typelen == 0) {
				if (rel.length() == 0) {
					return null;
				}
			} else if (!"text/css".equalsIgnoreCase(type)) {
				return null;
			}
			byte relAttr = AbstractCSSStyleSheet.parseRelAttribute(rel);
			if (relAttr != -1) {
				String title = element.getAttribute("title").trim();
				if (title.length() == 0) {
					title = null;
				}
				String href = element.getAttribute("href");
				if (href.length() != 0) {
					if (relAttr == 0) {
						if (loadLinkedStyleSheet(href, title)) {
							needsUpdate = false;
						}
					} else {
						if (title != null) {
							if (href.length() != 0) {
								// Disable this alternate sheet if it is a new sheet
								// or is not the selected set
								boolean disable = definedSheet == null || !title
										.equalsIgnoreCase(element.getOwnerDocument().getSelectedStyleSheetSet());
								if (loadLinkedStyleSheet(href, title)) {
									// It is an alternate sheet
									if (disable) {
										definedSheet.setDisabled(true);
									}
									needsUpdate = false;
								}
							}
						} else {
							element.getOwnerDocument().getErrorHandler().linkedStyleError(element,
									"Alternate sheet without title");
						}
					}
				} else {
					element.getOwnerDocument().getErrorHandler().linkedStyleError(element,
							"Missing or void href attribute.");
				}
			} else {
				definedSheet = null;
			}
		}
		return definedSheet;
	}

	private boolean loadLinkedStyleSheet(String href, String title) {
		MediaQueryList media = element.getOwnerDocument().parseMediaList(element.getAttribute("media").trim(), element);
		if (media == null) {
			definedSheet = null;
			return false;
		}
		definedSheet = element.getOwnerDocument().loadStyleSheet(definedSheet, href, title, media, element);
		return true;
	}

	void resetSheet() {
		// Local reference to sheet, to avoid race conditions.
		final AbstractCSSStyleSheet sheet = definedSheet;
		if (sheet != null) {
			sheet.getCssRules().clear();
		}
		needsUpdate = true;
		element.getOwnerDocument().onSheetModify();
	}

	void postAddChildInline(AbstractDOMNode newChild) {
		// If newChild is not the only child, reset sheet
		if (element.getFirstChild().getNextSibling() != null) {
			resetSheet();
			getInlineSheet();
		}
	}

	boolean containsCSS() {
		if (definedSheet != null) {
			// Local reference to sheet, to avoid race conditions.
			final AbstractCSSStyleSheet sheet;
			String type = element.getAttribute("type");
			if ("text/css".equalsIgnoreCase(type) || (type.length() == 0 && (sheet = getInlineSheet()) != null
					&& sheet.getCssRules().getLength() != 0)) {
				return true;
			}
		}
		/*
		 * If the sheet has not been processed yet, we return false as well.
		 */
		return false;
	}

}
