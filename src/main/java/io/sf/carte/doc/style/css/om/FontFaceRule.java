/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.agent.DeviceFactory;
import io.sf.carte.doc.style.css.CSSFontFaceRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.SelectorMatcher;
import io.sf.carte.doc.style.css.StyleDatabase;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.om.BaseDocumentCSSStyleSheet.Cascade;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSFontFaceRule.
 */
public class FontFaceRule extends BaseCSSDeclarationRule implements CSSFontFaceRule {

	private static final long serialVersionUID = 1L;

	public FontFaceRule(AbstractCSSStyleSheet parentSheet, int origin) {
		super(parentSheet, CSSRule.FONT_FACE_RULE, origin);
	}

	@Override
	void cascade(Cascade cascade, SelectorMatcher matcher, String targetMedium) {
		DeviceFactory df = getParentStyleSheet().getStyleSheetFactory().getDeviceFactory();
		if (df != null) {
			StyleDatabase sdb = df.getStyleDatabase(targetMedium);
			if (sdb != null) {
				sdb.loadFontFaceRule(this);
			}
		}
	}

	@Override
	public String getCssText() {
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(50 + getStyle().getLength() * 24);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText() {
		if (getStyle().getLength() != 0) {
			StringBuilder buf = new StringBuilder(128);
			buf.append("@font-face{").append(getStyle().getMinifiedCssText()).append('}');
			return buf.toString();
		}
		return "";
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		if (getStyle().getLength() != 0) {
			context.startRule(wri, getPrecedingComments());
			wri.write("@font-face");
			context.updateContext(this);
			context.writeLeftCurlyBracket(wri);
			context.startStyleDeclaration(wri);
			getStyle().writeCssText(wri, context);
			context.endCurrentContext(this);
			context.endStyleDeclaration(wri);
			context.writeRightCurlyBracket(wri);
			context.endRule(wri, getTrailingComments());
		}
	}

}
