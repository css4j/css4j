/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.CSSValue.Type;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.PageSelectorList;
import io.sf.carte.doc.style.css.nsac.ParserControl;
import io.sf.carte.doc.style.css.nsac.SelectorList;
import io.sf.carte.doc.style.css.om.BaseCSSStyleDeclaration;
import io.sf.carte.doc.style.css.om.DefaultStyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.LexicalValue;
import io.sf.carte.doc.style.css.property.ShorthandDatabase;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.doc.style.css.util.Minify.Config;
import io.sf.carte.util.BufferSimpleWriter;

class MinifySheetHandler implements CSSHandler {

	/*
	 * There is no constant in CSSRule for feature maps, so create one.
	 */
	private static final short FEATURE_MAP = 63;

	private boolean ignoreImports = false;

	private LinkedList<Short> currentRule = new LinkedList<>();

	private BitSet ruleContent = new BitSet();

	private ValueFactory factory = new ValueFactory();

	private BufferSimpleWriter writer;

	private StringBuilder ruleBuf = new StringBuilder(128);

	private final Config config;

	private final char preserveCommentChar;

	MinifySheetHandler(BufferSimpleWriter wri, Config config) {
		super();
		this.writer = wri;
		this.config = config;
		this.preserveCommentChar = config.getPreserveCommentChar();
	}

	@Override
	public void parseStart(ParserControl parserctl) {
		// Starting StyleSheet processing
		ignoreImports = false;
	}

	@Override
	public void endOfStream() {
		// Ending StyleSheet processing
	}

	private void write(char c) {
		writer.write(c);
	}

	private void write(CharSequence string) {
		writer.write(string);
	}

	private void writeBuf() {
		if (ruleBuf.length() > 0) {
			write(ruleBuf);
			ruleBuf.setLength(0);
		}
	}

	private void appendBuf(char c) {
		ruleBuf.append(c);
	}

	private void appendBuf(CharSequence seq) {
		ruleBuf.append(seq);
	}

	@Override
	public void comment(String text, boolean precededByLF) {
		if (!text.isEmpty() && text.charAt(0) == preserveCommentChar) {
			appendBuf("/*");
			appendBuf(text);
			appendBuf("*/");
			// This comment is important and counts as rule content
			setRuleContent();
		}
	}

	@Override
	public void ignorableAtRule(String atRule) {
		writeBuf();
		setRuleContent();
		// Ignorable @-rule
		try {
			Minify.shallowMinify(new StringReader(atRule), ruleBuf);
		} catch (IOException e) {
			// Cannot happen with StringReader
		}
		write(ruleBuf);
		ruleBuf.setLength(0);
	}

	@Override
	public void namespaceDeclaration(String prefix, String uri) {
		writeBuf();
		setRuleContent();

		write("@namespace ");
		if (prefix != null && !prefix.isEmpty()) {
			write(prefix);
			write(' ');
		}
		writeURL(uri);
		write(';');
	}

	private void writeURL(String uri) {
		String quoted = ParseHelper.quote(uri, '"');
		write(quoted);
	}

	@Override
	public void importStyle(String uri, String layer, BooleanCondition supportsCondition,
			MediaQueryList media, String defaultNamespaceURI) throws CSSException {
		// Ignore any '@import' rule that occurs inside a block or after any
		// non-ignored statement other than an @charset or an @import rule
		// (CSS 2.1 §4.1.5)
		if (ignoreImports) {
			return;
		}

		checkMediaErrors(media);

		writeBuf();

		write("@import ");

		writeURL(uri);

		if (layer != null) {
			write(' ');
			write("layer");
			if (!layer.isEmpty()) {
				write('(');
				write(layer);
				write(')');
			}
		}

		if (supportsCondition != null) {
			write(' ');
			write("supports");
			write('(');
			supportsCondition.appendMinifiedText(ruleBuf);
			write(ruleBuf);
			ruleBuf.setLength(0);
			write(')');
		}

		if (media != null) {
			write(' ');
			write(media.getMinifiedMedia());
		}

		write(';');
	}

	private void checkMediaErrors(MediaQueryList media) throws CSSException {
		if (media.hasErrors()) {
			List<CSSParseException> exs = media.getExceptions();
			if (exs != null) {
				throw exs.get(0);
			}
			throw new CSSException("Media query has errors.");
		}
	}

	@Override
	public void startSupports(BooleanCondition condition) {
		// Starting @supports block
		ignoreImports = true;
		currentRule.add(CSSRule.SUPPORTS_RULE);
		writeBuf();

		appendBuf("@supports ");
		condition.appendMinifiedText(ruleBuf);
		appendBuf('{');
	}

	@Override
	public void endSupports(BooleanCondition condition) {
		endDeclarationsRule();
	}

	@Override
	public void startMedia(MediaQueryList media) throws CSSException {
		checkMediaErrors(media);

		// Starting @media block
		ignoreImports = true;
		currentRule.add(CSSRule.MEDIA_RULE);
		writeBuf();

		appendBuf("@media");

		if (media != null) {
			appendBuf(' ');
			appendBuf(media.getMinifiedMedia());
		}
		appendBuf('{');
	}

	@Override
	public void endMedia(MediaQueryList media) {
		endDeclarationsRule();
	}

	@Override
	public void startPage(PageSelectorList pageSelectorList) {
		ignoreImports = true;
		writeBuf();
		currentRule.add(CSSRule.PAGE_RULE);

		appendBuf("@page");
		if (pageSelectorList != null) {
			appendBuf(' ');
			appendBuf(pageSelectorList.toString());
		}
		appendBuf('{');
	}

	@Override
	public void endPage(PageSelectorList pageSelectorList) {
		endDeclarationsRule();
	}

	@Override
	public void startMargin(String name) {
		writeBuf();
		currentRule.add(CSSRule.MARGIN_RULE);

		appendBuf('@');
		appendBuf(name);
		appendBuf('{');
	}

	@Override
	public void endMargin() {
		endDeclarationsRule();
	}

	@Override
	public void startFontFace() {
		ignoreImports = true;
		writeBuf();
		currentRule.add(CSSRule.FONT_FACE_RULE);

		appendBuf("@font-face{");
	}

	@Override
	public void endFontFace() {
		endDeclarationsRule();
	}

	@Override
	public void startCounterStyle(String name) {
		ignoreImports = true;
		writeBuf();
		currentRule.add(CSSRule.COUNTER_STYLE_RULE);

		appendBuf("@counter-style ");
		appendBuf(name);
		appendBuf('{');
	}

	@Override
	public void endCounterStyle() {
		endDeclarationsRule();
	}

	@Override
	public void startKeyframes(String name) {
		ignoreImports = true;
		writeBuf();
		currentRule.add(CSSRule.KEYFRAMES_RULE);

		appendBuf("@keyframes ");
		appendBuf(name);
		appendBuf('{');
	}

	@Override
	public void endKeyframes() {
		endGenericRule(CSSRule.KEYFRAMES_RULE);
	}

	@Override
	public void startKeyframe(LexicalUnit keyframeSelector) {
		writeBuf();
		setRuleContent(CSSRule.KEYFRAMES_RULE);
		currentRule.add(CSSRule.PAGE_RULE);

		miniKeyframeSelector(keyframeSelector);
		appendBuf('{');
	}

	private void miniKeyframeSelector(LexicalUnit selunit) {
		appendMiniSelector(selunit);
		LexicalUnit lu = selunit.getNextLexicalUnit();
		while (lu != null) {
			LexicalUnit nextlu = lu.getNextLexicalUnit();
			ruleBuf.append(',');
			appendMiniSelector(nextlu);
			lu = nextlu.getNextLexicalUnit();
		}
	}

	private void appendMiniSelector(LexicalUnit selunit) {
		LexicalType type = selunit.getLexicalUnitType();
		if (type == LexicalType.IDENT || type == LexicalType.STRING) {
			ruleBuf.append(selunit.getStringValue());
		} else if (type == LexicalType.PERCENTAGE) {
			float floatValue = selunit.getFloatValue();
			if (floatValue == 0f) {
				ruleBuf.append('0');
				return;
			}
			if (floatValue % 1 != 0) {
				ruleBuf.append(String.format(Locale.ROOT, "%s", floatValue));
			} else {
				ruleBuf.append(String.format(Locale.ROOT, "%.0f", floatValue));
			}
			ruleBuf.append('%');
		} else if (type == LexicalType.INTEGER && selunit.getIntegerValue() == 0) {
			ruleBuf.append('0');
		} else {
			ruleBuf.append(selunit.getCssText());
		}
	}

	@Override
	public void endKeyframe() {
		endDeclarationsRule();
	}

	@Override
	public void startFontFeatures(String[] familyName) {
		ignoreImports = true;
		writeBuf();
		currentRule.add(CSSRule.FONT_FEATURE_VALUES_RULE);

		appendBuf("@font-feature-values ");
		appendBuf(familyName[0]);
		for (int i = 1; i < familyName.length; i++) {
			appendBuf(',');
			appendBuf(familyName[i]);
		}
		appendBuf('{');
	}

	@Override
	public void endFontFeatures() {
		endGenericRule(CSSRule.FONT_FEATURE_VALUES_RULE);
	}

	@Override
	public void startFeatureMap(String mapName) {
		writeBuf();
		setRuleContent(CSSRule.FONT_FEATURE_VALUES_RULE);
		currentRule.add(FEATURE_MAP);

		appendBuf('@');
		appendBuf(mapName);
		appendBuf('{');
	}

	@Override
	public void endFeatureMap() {
		endDeclarationsRule();
	}

	@Override
	public void startProperty(String name) {
		ignoreImports = true;
		writeBuf();
		currentRule.add(CSSRule.PROPERTY_RULE);

		appendBuf("@property ");
		appendBuf(name);
		appendBuf('{');
	}

	@Override
	public void endProperty(boolean discard) {
		endDeclarationsRule();
	}

	@Override
	public void startSelector(SelectorList selectors) {
		ignoreImports = true;
		writeBuf();
		currentRule.add(CSSRule.STYLE_RULE);

		config.serializeSelectors(selectors, ruleBuf);
		appendBuf('{');
	}

	@Override
	public void endSelector(SelectorList selectors) {
		endDeclarationsRule();
	}

	private void endDeclarationsRule() {
		short idx = currentRule.removeLast().shortValue();
		if (ruleContent.get(idx)) {
			// The rule is not empty
			if (!currentRule.contains(idx)) {
				// Not nested inside another rule of same type
				ruleContent.set(idx, false);
			}
			int len = ruleBuf.length();
			if (len > 0) {
				char last = ruleBuf.charAt(len - 1);
				if (last == ';') {
					ruleBuf.setLength(len - 1);
				}
			}
			appendBuf('}');
			write(ruleBuf);
		}
		ruleBuf.setLength(0);
	}

	private void endGenericRule(short ruleId) {
		if (ruleContent.get(ruleId)) {
			// The rule is not empty
			if (!currentRule.contains(ruleId)) {
				// Not nested inside another rule of same type
				ruleContent.set(ruleId, false);
			}
			appendBuf('}');
			write(ruleBuf);
		}
		ruleBuf.setLength(0);
	}

	@Override
	public void property(String name, LexicalUnit value, boolean important) {
		setRuleContent();

		appendBuf(name);
		appendBuf(':');

		if (ShorthandDatabase.getInstance().isShorthand(name)
				&& !config.isDisabledShorthand(name)) {
			StringBuilder buf = new StringBuilder(32);
			serializeValue(name, value, buf, false);

			DefaultStyleDeclarationErrorHandler eh = new DefaultStyleDeclarationErrorHandler();
			BaseCSSStyleDeclaration style = new BaseCSSStyleDeclaration() {

				private static final long serialVersionUID = 1L;

				@Override
				public StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
					return eh;
				}

			};

			CharSequence seq = buf;
			try {
				style.setProperty(name, value, important);
				if (!style.getStyleDeclarationErrorHandler().hasErrors()) {
					String decl = style.getMinifiedPropertyValue(name);
					if (buf.length() > decl.length() && !decl.isEmpty()) {
						seq = decl;
					}
				}
			} catch (Exception e) {
			}
			appendBuf(seq);
		} else {
			serializeValue(name, value, ruleBuf, false);
		}

		if (important) {
			appendBuf("!important");
		}

		appendBuf(';');
	}

	/**
	 * Set that the given rule contains content.
	 */
	private void setRuleContent(short ruleId) {
		ruleContent.set(ruleId);
	}

	/**
	 * Set that the current rule(s) contains content.
	 */
	private void setRuleContent() {
		for (short ruleId : currentRule) {
			ruleContent.set(ruleId);
		}
	}

	private void serializeValue(String propertyName, LexicalUnit value, StringBuilder buf,
			boolean keepZeroUnit) {
		String mini = LexicalValue.serializeMinifiedSequence(value, propertyName, keepZeroUnit);
		try {
			StyleValue omvalue = factory.createCSSValue(value);
			// If it is LEXICAL it will repeat the serializeMinifiedSequence()
			if (omvalue.getPrimitiveType() != Type.LEXICAL) {
				String ommini = omvalue.getMinifiedCssText();
				if (ommini.length() < mini.length()) {
					config.serializeValue(propertyName, ommini, buf);
					return;
				}
			}
		} catch (Exception e) {
		}

		config.serializeValue(propertyName, mini, buf);
	}

	@Override
	public void lexicalProperty(String name, LexicalUnit lunit, boolean important) {
		setRuleContent();

		appendBuf(name);
		appendBuf(':');

		serializeValue("", lunit, ruleBuf, true);

		if (important) {
			appendBuf("!important");
		}

		appendBuf(';');
	}

}
