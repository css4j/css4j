/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.Iterator;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSMediaRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSMediaRule.
 * 
 */
public class MediaRule extends GroupingRule implements CSSMediaRule {

	private static final long serialVersionUID = 1L;

	private MediaQueryList mediaList = null;

	protected MediaRule(AbstractCSSStyleSheet parentSheet, MediaQueryList mediaList, byte origin) {
		super(parentSheet, CSSRule.MEDIA_RULE, origin);
		cssRules = new CSSRuleArrayList();
		this.mediaList = mediaList;
	}

	MediaRule(AbstractCSSStyleSheet parentSheet, MediaRule copyfrom) {
		super(parentSheet, copyfrom);
		this.mediaList = ((MediaListAccess) copyfrom.getMedia()).unmodifiable();
	}

	@Override
	public MediaQueryList getMedia() {
		return mediaList;
	}

	@Override
	public String getCssText() {
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(64 + getCssRules().getLength() * 36);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText() {
		StringBuilder sb = new StringBuilder(30 + getCssRules().getLength() * 20);
		sb.append("@media");
		if (!mediaList.isAllMedia()) {
			sb.append(' ').append(mediaList.getMinifiedMedia());
		}
		sb.append("{");
		Iterator<AbstractCSSRule> it = getCssRules().iterator();
		while (it.hasNext()) {
			sb.append(it.next().getMinifiedCssText());
		}
		sb.append('}');
		return sb.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		context.startRule(wri, getPrecedingComments());
		wri.write("@media");
		if (!mediaList.isAllMedia()) {
			wri.write(' ');
			wri.write(mediaList.getMedia());
		}
		context.updateContext(this);
		context.writeLeftCurlyBracket(wri);
		getCssRules().writeCssText(wri, context);
		context.endCurrentContext(this);
		context.endRuleList(wri);
		context.writeRightCurlyBracket(wri);
		context.endRule(wri, getTrailingComments());
	}

	@Override
	boolean hasErrorsOrWarnings() {
		return super.hasErrorsOrWarnings() || (mediaList != null && mediaList.hasErrors());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mediaList == null) ? 0 : mediaList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MediaRule other = (MediaRule) obj;
		if (mediaList == null) {
			if (other.mediaList != null) {
				return false;
			}
		} else if (!mediaList.equals(other.mediaList)) {
			return false;
		}
		return true;
	}

	@Override
	public MediaRule clone(AbstractCSSStyleSheet parentSheet) {
		return new MediaRule(parentSheet, this);
	}

}
