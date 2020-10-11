/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.nsac.LexicalUnit.LexicalType;
import io.sf.carte.doc.style.css.property.LexicalValue;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.UnknownValue;
import io.sf.carte.util.SimpleWriter;

/**
 * Contains a set of non-conformant values (that provide compatibility with non-compliant
 * browsers).
 */
class CompatDeclarationSet implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private final HashMap<String, StyleValue> overrideMap = new HashMap<String, StyleValue>();
	private final HashMap<String, Boolean> overridePrio = new HashMap<String, Boolean>();
	private final HashMap<String, StyleValue> nonOverrideMap = new HashMap<String, StyleValue>();
	private final HashMap<String, ShorthandValue> compatShorthandMap = new HashMap<String, ShorthandValue>();
	private final HashMap<String, ShorthandValue> nonOvShorthandMap = new HashMap<String, ShorthandValue>();

	CompatDeclarationSet() {
		super();
	}

	CompatDeclarationSet(CompatDeclarationSet copiedObject) {
		super();
		overrideMap.putAll(copiedObject.overrideMap);
		overridePrio.putAll(copiedObject.overridePrio);
		nonOverrideMap.putAll(copiedObject.nonOverrideMap);
		compatShorthandMap.putAll(copiedObject.compatShorthandMap);
		nonOvShorthandMap.putAll(copiedObject.nonOvShorthandMap);
	}

	void setCompatLonghand(String propertyName, StyleValue override, boolean priorityImportant,
			boolean isOverride) {
		if (isOverride) {
			overrideMap.put(propertyName, override);
			overridePrio.put(propertyName, priorityImportant);
			nonOverrideMap.remove(propertyName);
		} else if (isPriorityCompat(override)) {
			nonOverrideMap.put(propertyName, override);
		}
	}

	boolean setCompatShorthand(String propertyName, LexicalUnit value, boolean important,
			LinkedList<String> shadowedShorthands) {
		if (containsIdentCompat(value)) {
			String cssText = value.toString();
			ShorthandValue shorthand = new ShorthandValue(value, important);
			shorthand.setShorthandText(cssText, LexicalValue.serializeMinifiedSequence(value));
			if (shadowedShorthands != null && shadowedShorthands.contains(propertyName)) {
				compatShorthandMap.put(propertyName, shorthand);
				nonOvShorthandMap.remove(propertyName);
			} else if (shorthand.isPriorityCompat()) {
				nonOvShorthandMap.put(propertyName, shorthand);
			}
			return true;
		}
		return false;
	}

	private static boolean containsIdentCompat(LexicalUnit value) {
		while (value != null) {
			LexicalType type = value.getLexicalUnitType();
			if (type == LexicalType.COMPAT_IDENT || type == LexicalType.COMPAT_PRIO) {
				return true;
			} else if (type == LexicalType.FUNCTION) {
				return containsIdentCompat(value.getParameters());
			} else if (type == LexicalType.SUB_EXPRESSION) {
				return containsIdentCompat(value.getSubValues());
			}
			value = value.getNextLexicalUnit();
		}
		return false;
	}

	static boolean isPriorityCompat(StyleValue compatvalue) {
		return compatvalue.getPrimitiveType() == CSSValue.Type.UNKNOWN
				&& ((UnknownValue) compatvalue).isPriorityCompat();
	}

	static void appendIEPrioCharShorthandMinifiedCssText(StringBuilder sb, String shorthandName,
			ShorthandValue compatvalue) {
		sb.append(shorthandName).append(':').append(compatvalue.getMinifiedCssText(shorthandName));
		sb.append("!important!;");
	}

	static void writeIEPrioCharShorthandCssText(SimpleWriter wri, StyleFormattingContext context, String shorthandName,
			ShorthandValue compatvalue) throws IOException {
		wri.write(shorthandName);
		context.writeColon(wri);
		wri.write(compatvalue.getCssText());
		wri.write("!important!");
		context.writeSemiColon(wri);
	}

	static void writeIEPrioCharLonghandCssText(SimpleWriter wri, StyleFormattingContext context, String ptyname,
			StyleValue compatvalue) throws IOException {
		wri.write(ptyname);
		context.writeColon(wri);
		compatvalue.writeCssText(wri);
		wri.write("!important!");
		context.writeSemiColon(wri);
	}

	ShorthandValue getCompatShorthand(String shorthandName) {
		return compatShorthandMap.get(shorthandName);
	}

	boolean isCompatShorthand(String shorthandName) {
		return compatShorthandMap.containsKey(shorthandName);
	}

	boolean isCompatLonghand(String ptyname) {
		return overrideMap.containsKey(ptyname);
	}

	StyleValue getCompatLonghand(String ptyname) {
		return overrideMap.get(ptyname);
	}

	boolean isImportantCompatLonghand(String ptyname) {
		return overridePrio.get(ptyname);
	}

	ShorthandValue getNonOvCompatShorthand(String shorthandName) {
		return nonOvShorthandMap.get(shorthandName);
	}

	StyleValue getNonOvCompatLonghand(String ptyname) {
		return nonOverrideMap.get(ptyname);
	}

	public void clear() {
		overrideMap.clear();
		overridePrio.clear();
		nonOverrideMap.clear();
		compatShorthandMap.clear();
		nonOvShorthandMap.clear();
	}

	@Override
	public CompatDeclarationSet clone() {
		return new CompatDeclarationSet(this);
	}

}
