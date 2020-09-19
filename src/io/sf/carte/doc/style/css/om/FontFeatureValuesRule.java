/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.CSSFontFeatureValuesMap;
import io.sf.carte.doc.style.css.CSSFontFeatureValuesRule;
import io.sf.carte.doc.style.css.CSSRule;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.CSSException;
import io.sf.carte.doc.style.css.nsac.CSSHandler;
import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.nsac.Locator;
import io.sf.carte.doc.style.css.nsac.ParserControl;
import io.sf.carte.doc.style.css.parser.CSSParser;
import io.sf.carte.doc.style.css.parser.CommentRemover;
import io.sf.carte.doc.style.css.parser.EmptyCSSHandler;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.NumberValue;
import io.sf.carte.doc.style.css.property.PrimitiveValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.ValueFactory;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * Implementation of CSSFontFeatureValuesRule.
 * 
 * @author Carlos Amengual
 * 
 */
public class FontFeatureValuesRule extends BaseCSSRule implements CSSFontFeatureValuesRule {

	private String[] fontFamily = null;
	private CSSFontFeatureValuesMapImpl annotation = new CSSFontFeatureValuesMapImpl();
	private CSSFontFeatureValuesMapImpl ornaments = new CSSFontFeatureValuesMapImpl();
	private CSSFontFeatureValuesMapImpl stylistic = new CSSFontFeatureValuesMapImpl();
	private CSSFontFeatureValuesMapImpl swash = new CSSFontFeatureValuesMapImpl();
	private CSSFontFeatureValuesMapImpl characterVariant = new CSSFontFeatureValuesMapImpl();
	private CSSFontFeatureValuesMapImpl styleset = new CSSFontFeatureValuesMapImpl();
	// Non-standard maps
	private HashMap<String, CSSFontFeatureValuesMapImpl> mapmap = null;

	protected FontFeatureValuesRule(AbstractCSSStyleSheet parentSheet, byte origin) {
		super(parentSheet, CSSRule.FONT_FEATURE_VALUES_RULE, origin);
	}

	@Override
	public String[] getFontFamily() {
		return fontFamily;
	}

	void setFontFamily(String[] fontFamily) {
		this.fontFamily = fontFamily;
	}

	@Override
	public CSSFontFeatureValuesMap getAnnotation() {
		return annotation;
	}

	@Override
	public CSSFontFeatureValuesMap getOrnaments() {
		return ornaments;
	}

	@Override
	public CSSFontFeatureValuesMap getStylistic() {
		return stylistic;
	}

	@Override
	public CSSFontFeatureValuesMap getSwash() {
		return swash;
	}

	@Override
	public CSSFontFeatureValuesMap getCharacterVariant() {
		return characterVariant;
	}

	@Override
	public CSSFontFeatureValuesMap getStyleset() {
		return styleset;
	}

	/**
	 * Enable a feature values map for the given feature value name. When enabled, a map for
	 * <code>featureValueName</code> will be returned by {@link #getFeatureValuesMap(String)}.
	 * <p>
	 * If a standard feature values name (like 'annotation' or 'stylistic') is enabled, this
	 * method has no effect. The method can be used to enable, for example,
	 * <code>historical-forms</code>.
	 * 
	 * @param featureValueName
	 *            the feature value name to be enabled.
	 */
	@Override
	public void enableMap(String featureValueName) {
		featureValueName = featureValueName.toLowerCase(Locale.ROOT);
		enableMap(featureValueName, mapmap);
	}

	private static void enableMap(String featureValueName, HashMap<String, CSSFontFeatureValuesMapImpl> mapmap) {
		if (!featureValueName.equals("annotation") && !featureValueName.equals("ornaments") &&
				!featureValueName.equals("stylistic") && !featureValueName.equals("swash") &&
				!featureValueName.equals("character-variant") && !featureValueName.equals("styleset")) {
			if (mapmap == null) {
				mapmap = new HashMap<String, CSSFontFeatureValuesMapImpl>();
			}
			mapmap.put(featureValueName, new CSSFontFeatureValuesMapImpl());
		}
	}

	/**
	 * Get a feature values map for the given name.
	 * 
	 * @param featureValueName
	 *            the feature value name.
	 * @return the feature values map, or null if the feature value name is not standard and
	 *         has not been enabled by {@link #enableMap(String)}.
	 */
	@Override
	public CSSFontFeatureValuesMap getFeatureValuesMap(String featureValueName) {
		CSSFontFeatureValuesMap map;
		featureValueName = featureValueName.toLowerCase(Locale.ROOT);
		if (featureValueName.equals("annotation")) {
			map = annotation;
		} else if (featureValueName.equals("ornaments")) {
			map = ornaments;
		} else if (featureValueName.equals("stylistic")) {
			map = stylistic;
		} else if (featureValueName.equals("swash")) {
			map = swash;
		} else if (featureValueName.equals("character-variant")) {
			map = characterVariant;
		} else if (featureValueName.equals("styleset")) {
			map = styleset;
		} else if (mapmap == null) {
			map = null;
		} else {
			map = mapmap.get(featureValueName);
		}
		return map;
	}

	@Override
	public String getCssText() {
		StyleFormattingContext context = getStyleFormattingContext();
		context.setParentContext(getParentRule());
		BufferSimpleWriter sw = new BufferSimpleWriter(256);
		try {
			writeCssText(sw, context);
		} catch (IOException e) {
			throw new DOMException(DOMException.INVALID_STATE_ERR, e.getMessage());
		}
		return sw.toString();
	}

	@Override
	public String getMinifiedCssText() {
		if (fontFamily == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder(128);
		buf.append("@font-feature-values ");
		buf.append(fontFamily[0]);
		for (int i = 1; i < fontFamily.length; i++) {
			buf.append(',').append(fontFamily[i]);
		}
		buf.append('{');
		if (!swash.isEmpty()) {
			buf.append("@swash{");
			appendMinifiedFeatureString(buf, swash);
			buf.append('}');
		}
		if (!annotation.isEmpty()) {
			buf.append("@annotation{");
			appendMinifiedFeatureString(buf, annotation);
			buf.append('}');
		}
		if (!ornaments.isEmpty()) {
			buf.append("@ornaments{");
			appendMinifiedFeatureString(buf, ornaments);
			buf.append('}');
		}
		if (!stylistic.isEmpty()) {
			buf.append("@stylistic{");
			appendMinifiedFeatureString(buf, stylistic);
			buf.append('}');
		}
		if (!styleset.isEmpty()) {
			buf.append("@styleset{");
			appendMinifiedFeatureString(buf, styleset);
			buf.append('}');
		}
		if (!characterVariant.isEmpty()) {
			buf.append("@character-variant{");
			appendMinifiedFeatureString(buf, characterVariant);
			buf.append('}');
		}
		if (mapmap != null) {
			Iterator<Entry<String, CSSFontFeatureValuesMapImpl>> it = mapmap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, CSSFontFeatureValuesMapImpl> me = it.next();
				buf.append('@').append(me.getKey()).append('{');
				appendMinifiedFeatureString(buf, me.getValue());
				buf.append('}');
			}
		}
		buf.append('}');
		return buf.toString();
	}

	private void appendMinifiedFeatureString(StringBuilder buf, CSSFontFeatureValuesMapImpl featureMap) {
		int szm1 = featureMap.featureMap.size() - 1;
		String[] names = featureMap.featureMap.keySet().toArray(new String[0]);
		for (int i = 0; i <= szm1; i++) {
			String name = names[i];
			buf.append(name).append(':');
			PrimitiveValue[] values = featureMap.featureMap.get(name);
			buf.append(values[0].getMinifiedCssText(name));
			for (int j = 1; j < values.length; j++) {
				buf.append(' ').append(values[j].getMinifiedCssText(name));
			}
			if (i != szm1) {
				buf.append(';');
			}
		}
	}

	@Override
	public void writeCssText(SimpleWriter wri, StyleFormattingContext context) throws IOException {
		if (fontFamily == null) {
			return;
		}
		context.startRule(wri, getPrecedingComments());
		wri.write("@font-feature-values ");
		writeFontFamily(wri, fontFamily[0]);
		for (int i = 1; i < fontFamily.length; i++) {
			context.writeComma(wri);
			writeFontFamily(wri, fontFamily[i]);
		}
		context.updateContext(this);
		context.writeLeftCurlyBracket(wri);
		writeFeatureBlock(wri, context, "@swash", swash);
		writeFeatureBlock(wri, context, "@annotation", annotation);
		writeFeatureBlock(wri, context, "@ornaments", ornaments);
		writeFeatureBlock(wri, context, "@stylistic", stylistic);
		writeFeatureBlock(wri, context, "@styleset", styleset);
		writeFeatureBlock(wri, context, "@character-variant", characterVariant);
		if (mapmap != null) {
			Iterator<Entry<String, CSSFontFeatureValuesMapImpl>> it = mapmap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, CSSFontFeatureValuesMapImpl> me = it.next();
				CSSFontFeatureValuesMapImpl ffvmap = me.getValue();
				context.startRule(wri, ffvmap.precedingComments);
				wri.write('@');
				wri.write(me.getKey());
				context.writeLeftCurlyBracket(wri);
				appendFeatureString(wri, context, ffvmap);
				context.writeRightCurlyBracket(wri);
				context.endRule(wri, ffvmap.trailingComments);
			}
		}
		context.endCurrentContext(this);
		context.writeRightCurlyBracket(wri);
		context.endRule(wri, getTrailingComments());
	}

	private static void writeFontFamily(SimpleWriter wri, String ff) throws IOException {
		if (ff.indexOf(' ') == -1) {
			wri.write(ff);
		} else {
			wri.write('\'');
			wri.write(ff);
			wri.write('\'');
		}
	}

	private void writeFeatureBlock(SimpleWriter wri, StyleFormattingContext context, String atFeatureType,
			CSSFontFeatureValuesMapImpl featureMap) throws IOException {
		if (!featureMap.isEmpty()) {
			context.startRule(wri, featureMap.precedingComments);
			wri.write(atFeatureType);
			context.writeLeftCurlyBracket(wri);
			appendFeatureString(wri, context, featureMap);
			context.writeRightCurlyBracket(wri);
			context.endRule(wri, featureMap.trailingComments);
		}
	}

	private void appendFeatureString(SimpleWriter wri, StyleFormattingContext context,
			CSSFontFeatureValuesMapImpl featureMap) throws IOException {
		context.deepenCurrentContext();
		context.startStyleDeclaration(wri);
		Iterator<Entry<String, PrimitiveValue[]>> it = featureMap.featureMap.entrySet().iterator();
		while (it.hasNext()) {
			context.startPropertyDeclaration(wri);
			Entry<String, PrimitiveValue[]> me = it.next();
			wri.write(me.getKey());
			context.writeColon(wri);
			PrimitiveValue[] values = me.getValue();
			values[0].writeCssText(wri);
			for (int i = 1; i < values.length; i++) {
				wri.write(' ');
				values[i].writeCssText(wri);
			}
			context.writeSemiColon(wri);
			context.endPropertyDeclaration(wri);
		}
		context.updateContext(this);
		context.endStyleDeclaration(wri);
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		cssText = cssText.trim();
		int len = cssText.length();
		int atIdx = cssText.indexOf('@');
		if (len < 24 || atIdx == -1) {
			throw new DOMException(DOMException.SYNTAX_ERR, "Invalid @font-feature-values rule: " + cssText);
		}
		String ncText = CommentRemover.removeComments(cssText).toString().trim();
		CharSequence atkeyword = ncText.subSequence(0, 21);
		if (!ParseHelper.startsWithIgnoreCase(atkeyword, "@font-feature-values")
				|| !Character.isWhitespace((atkeyword.charAt(20)))) {
			throw new DOMException(DOMException.INVALID_MODIFICATION_ERR,
					"Not a @font-feature-values rule: " + cssText);
		}
		String body = cssText.substring(atIdx + 21);
		CSSHandler handler = new MyFontFeatureValuesHandler();
		CSSParser parser = (CSSParser) createSACParser();
		parser.setDocumentHandler(handler);
		try {
			parser.parseFontFeatureValuesBody(body);
		} catch (CSSParseException e) {
			throw new DOMException(DOMException.INVALID_CHARACTER_ERR, e.getMessage());
		}
	}

	CSSHandler createFontFeatureValuesHandler(CSSParentHandler parentHandler,
			ParserControl parserctl) {
		return new MyFontFeatureValuesHandler(parentHandler, parserctl);
	}

	private class MyFontFeatureValuesHandler extends EmptyCSSHandler {

		private String[] fontFamily = null;
		private final CSSFontFeatureValuesMapImpl annotation = new CSSFontFeatureValuesMapImpl();
		private final CSSFontFeatureValuesMapImpl ornaments = new CSSFontFeatureValuesMapImpl();
		private final CSSFontFeatureValuesMapImpl stylistic = new CSSFontFeatureValuesMapImpl();
		private final CSSFontFeatureValuesMapImpl swash = new CSSFontFeatureValuesMapImpl();
		private final CSSFontFeatureValuesMapImpl characterVariant = new CSSFontFeatureValuesMapImpl();
		private final CSSFontFeatureValuesMapImpl styleset = new CSSFontFeatureValuesMapImpl();
		private final HashMap<String, CSSFontFeatureValuesMapImpl> mapmap = null;

		private CSSFontFeatureValuesMapImpl currentMap = null;

		private CSSFontFeatureValuesMapImpl lastMap = null;

		private LinkedList<String> comments = null;

		private final CSSParentHandler parentHandler;

		private final ParserControl parserctl;

		private MyFontFeatureValuesHandler() {
			this(null, null);
		}

		private MyFontFeatureValuesHandler(CSSParentHandler parentHandler, ParserControl parserctl) {
			super();
			this.parentHandler = parentHandler;
			this.parserctl = parserctl;
		}

		@Override
		public void startFontFeatures(String[] fontFamily) {
			this.fontFamily = fontFamily;
		}

		@Override
		public void endFontFeatures() {
			if (fontFamily != null) {
				FontFeatureValuesRule.this.fontFamily = fontFamily;
				FontFeatureValuesRule.this.annotation.clear();
				FontFeatureValuesRule.this.ornaments.clear();
				FontFeatureValuesRule.this.stylistic.clear();
				FontFeatureValuesRule.this.swash.clear();
				FontFeatureValuesRule.this.characterVariant.clear();
				FontFeatureValuesRule.this.styleset.clear();
				FontFeatureValuesRule.this.annotation.addAll(annotation);
				FontFeatureValuesRule.this.ornaments.addAll(ornaments);
				FontFeatureValuesRule.this.stylistic.addAll(stylistic);
				FontFeatureValuesRule.this.swash.addAll(swash);
				FontFeatureValuesRule.this.characterVariant.addAll(characterVariant);
				FontFeatureValuesRule.this.styleset.addAll(styleset);
				FontFeatureValuesRule.this.mapmap = mapmap;
				// Comments
				FontFeatureValuesRule.this.annotation.setPrecedingComments(annotation.getPrecedingComments());
				FontFeatureValuesRule.this.ornaments.setPrecedingComments(ornaments.getPrecedingComments());
				FontFeatureValuesRule.this.stylistic.setPrecedingComments(stylistic.getPrecedingComments());
				FontFeatureValuesRule.this.swash.setPrecedingComments(swash.getPrecedingComments());
				FontFeatureValuesRule.this.characterVariant
						.setPrecedingComments(characterVariant.getPrecedingComments());
				FontFeatureValuesRule.this.styleset.setPrecedingComments(styleset.getPrecedingComments());
			}
			//
			if (parentHandler != null) {
				parentHandler.endSubHandler(CSSRule.FONT_FEATURE_VALUES_RULE);
			}
		}

		@Override
		public void startFeatureMap(String mapname) {
			mapname = mapname.toLowerCase(Locale.ROOT);
			enableMap(mapname);
			currentMap = getFeatureValuesMap(mapname);
		}

		private void enableMap(String featureValueName) {
			FontFeatureValuesRule.enableMap(featureValueName, mapmap);
			lastMap = null;
		}

		private CSSFontFeatureValuesMapImpl getFeatureValuesMap(String featureValueName) {
			CSSFontFeatureValuesMapImpl map;
			if (featureValueName.equals("annotation")) {
				map = annotation;
			} else if (featureValueName.equals("ornaments")) {
				map = ornaments;
			} else if (featureValueName.equals("stylistic")) {
				map = stylistic;
			} else if (featureValueName.equals("swash")) {
				map = swash;
			} else if (featureValueName.equals("character-variant")) {
				map = characterVariant;
			} else if (featureValueName.equals("styleset")) {
				map = styleset;
			} else if (mapmap == null) {
				map = null;
			} else {
				map = mapmap.get(featureValueName);
				map.precedingComments = null;
			}
			return map;
		}

		@Override
		public void endFeatureMap() {
			setCommentsToCurrentMap();
			lastMap = currentMap;
			currentMap = null;
		}

		@Override
		public void property(String name, LexicalUnit value, boolean important, int index) {
			LinkedList<PrimitiveValue> values = new LinkedList<PrimitiveValue>();
			for (; value != null; value = value.getNextLexicalUnit()) {
				LexicalType lutype = value.getLexicalUnitType();
				if (lutype == LexicalType.INTEGER) {
					NumberValue number = new NumberValue();
					int ival = value.getIntegerValue();
					number.setIntegerValue(ival);
					values.add(number);
					continue;
				} else if (lutype == LexicalType.VAR || lutype == LexicalType.CALC) {
					ValueFactory valueFactory = new ValueFactory();
					StyleValue cssval = valueFactory.createCSSValue(value);
					PrimitiveValue pri = (PrimitiveValue) cssval;
					pri.setExpectInteger();
					values.add(pri);
					continue;
				}
				String msg = "Found non-integer value: " + value.toString();
				if (parserctl != null) {
					Locator locator = parserctl.createLocator(index);
					CSSParseException ex = new CSSParseException(msg, locator);
					parserctl.getErrorHandler().error(ex);
				} else {
					throw new CSSException(msg);
				}
			}
			PrimitiveValue[] intvals = new PrimitiveValue[values.size()];
			for (int i = 0; i < intvals.length; i++) {
				intvals[i] = values.get(i);
			}
			currentMap.set(name, intvals);
		}

		@Override
		public void comment(String text, boolean precededByLF) {
			if (lastMap != null && !precededByLF) {
				if (lastMap.trailingComments == null) {
					lastMap.trailingComments = new LinkedList<String>();
				}
				lastMap.trailingComments.add(text);
			} else {
				if (currentMap == null) {
					if (comments == null) {
						comments = new LinkedList<String>();
					}
					comments.add(text);
				}
			}
		}

		private void setCommentsToCurrentMap() {
			if (comments != null && !comments.isEmpty()) {
				ArrayList<String> ruleComments = new ArrayList<String>(comments.size());
				ruleComments.addAll(comments);
				currentMap.setPrecedingComments(ruleComments);
			}
			resetCommentStack();
		}

		private void resetCommentStack() {
			if (comments != null) {
				comments.clear();
			}
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = ((annotation == null) ? 0 : annotation.hashCode());
		result = prime * result + ((characterVariant == null) ? 0 : characterVariant.hashCode());
		result = prime * result + Arrays.hashCode(fontFamily);
		result = prime * result + ((mapmap == null) ? 0 : mapmap.hashCode());
		result = prime * result + ((ornaments == null) ? 0 : ornaments.hashCode());
		result = prime * result + ((styleset == null) ? 0 : styleset.hashCode());
		result = prime * result + ((stylistic == null) ? 0 : stylistic.hashCode());
		result = prime * result + ((swash == null) ? 0 : swash.hashCode());
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
		FontFeatureValuesRule other = (FontFeatureValuesRule) obj;
		if (annotation == null) {
			if (other.annotation != null) {
				return false;
			}
		} else if (!annotation.equals(other.annotation)) {
			return false;
		}
		if (characterVariant == null) {
			if (other.characterVariant != null) {
				return false;
			}
		} else if (!characterVariant.equals(other.characterVariant)) {
			return false;
		}
		if (!Arrays.equals(fontFamily, other.fontFamily)) {
			return false;
		}
		if (mapmap == null) {
			if (other.mapmap != null) {
				return false;
			}
		} else if (!mapmap.equals(other.mapmap)) {
			return false;
		}
		if (ornaments == null) {
			if (other.ornaments != null) {
				return false;
			}
		} else if (!ornaments.equals(other.ornaments)) {
			return false;
		}
		if (styleset == null) {
			if (other.styleset != null) {
				return false;
			}
		} else if (!styleset.equals(other.styleset)) {
			return false;
		}
		if (stylistic == null) {
			if (other.stylistic != null) {
				return false;
			}
		} else if (!stylistic.equals(other.stylistic)) {
			return false;
		}
		if (swash == null) {
			if (other.swash != null) {
				return false;
			}
		} else if (!swash.equals(other.swash)) {
			return false;
		}
		return true;
	}

	@Override
	public FontFeatureValuesRule clone(AbstractCSSStyleSheet parentSheet) {
		FontFeatureValuesRule rule = new FontFeatureValuesRule(parentSheet, getOrigin());
		rule.fontFamily = fontFamily;
		rule.annotation = annotation;
		rule.ornaments = ornaments;
		rule.stylistic = stylistic;
		rule.swash = swash;
		rule.styleset = styleset;
		rule.characterVariant = characterVariant;
		return rule;
	}

	private static class CSSFontFeatureValuesMapImpl implements CSSFontFeatureValuesMap {

		private final LinkedHashMap<String, PrimitiveValue[]> featureMap = new LinkedHashMap<String, PrimitiveValue[]>();
		private List<String> precedingComments = null;
		private LinkedList<String> trailingComments = null;

		void addAll(CSSFontFeatureValuesMapImpl othermap) {
			featureMap.putAll(othermap.featureMap);
		}

		void clear() {
			featureMap.clear();
		}

		@Override
		public PrimitiveValue[] get(String featureValueName) {
			return featureMap.get(featureValueName);
		}

		@Override
		public void set(String featureValueName, PrimitiveValue... values) {
			if (values == null) {
				throw new DOMException(DOMException.SYNTAX_ERR, "Must provide at least one value");
			}
			for (PrimitiveValue pri : values) {
				if (pri == null) {
					throw new DOMException(DOMException.SYNTAX_ERR, "Null value supplied.");
				}
				pri.setExpectInteger();
			}
			featureMap.put(featureValueName, values);
		}

		public boolean isEmpty() {
			return featureMap.isEmpty();
		}

		void setPrecedingComments(List<String> ruleComments) {
			this.precedingComments  = ruleComments;
		}

		@Override
		public List<String> getPrecedingComments() {
			return precedingComments;
		}

		@Override
		public List<String> getTrailingComments() {
			return trailingComments;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			if (featureMap == null) {
				return 0;
			}
			int result = 1;
			TreeSet<String> set = new TreeSet<String>();
			set.addAll(featureMap.keySet());
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String ffname = it.next();
				result = prime * result + ffname.hashCode();
				result = prime * result + Arrays.hashCode(featureMap.get(ffname));
			}
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
			CSSFontFeatureValuesMapImpl other = (CSSFontFeatureValuesMapImpl) obj;
			if (featureMap == null) {
				if (other.featureMap != null) {
					return false;
				}
			} else if (other.featureMap == null || featureMap.size() != other.featureMap.size()) {
				return false;
			} else {
				Iterator<Entry<String, PrimitiveValue[]>> it = featureMap.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, PrimitiveValue[]> entry = it.next();
					String ffname = entry.getKey();
					PrimitiveValue[] values = entry.getValue();
					PrimitiveValue[] ovalues = other.featureMap.get(ffname);
					if (ovalues == null || !Arrays.equals(values, ovalues)) {
						return false;
					}
				}
			}
			return true;
		}

	}

}
