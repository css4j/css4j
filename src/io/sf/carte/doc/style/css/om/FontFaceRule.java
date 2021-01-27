/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSRule;

import io.sf.carte.doc.style.css.ExtendedCSSFontFaceRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSFontFaceRule.
 * 
 * @author Carlos Amengual
 * 
 */
public class FontFaceRule extends BaseCSSDeclarationRule implements ExtendedCSSFontFaceRule {

	public FontFaceRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.FONT_FACE_RULE, origin);
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
			context.startRule(wri);
			wri.write("@font-face");
			context.updateContext(this);
			context.writeLeftCurlyBracket(wri);
			context.startStyleDeclaration(wri);
			getStyle().writeCssText(wri, context);
			context.endCurrentContext(this);
			context.endStyleDeclaration(wri);
			context.writeRightCurlyBracket(wri);
			context.endRule(wri);
		}
	}

	@Override
	PropertyDocumentHandler createPropertyDocumentHandler() {
		return new FFDeclarationRuleDocumentHandler();
	}

	class FFDeclarationRuleDocumentHandler extends DeclarationRuleDocumentHandler {
		@Override
		public void startAtRule(String name, String pseudoSelector) {
			if (!"font-face".equalsIgnoreCase(name)) {
				throw new DOMException(DOMException.INVALID_MODIFICATION_ERR, "Cannot set rule of type: " + name);
			}
		}

	}
}
