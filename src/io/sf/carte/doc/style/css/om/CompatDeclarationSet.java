/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://carte.sourceforge.io/css4j/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.LexicalUnit2;
import io.sf.carte.doc.style.css.property.AbstractCSSValue;
import io.sf.carte.doc.style.css.property.CSSUnknownValue;
import io.sf.carte.util.SimpleWriter;

/**
 * Contains a set of non-conformant values (that provide compatibility with non-compliant
 * browsers).
 */
class CompatDeclarationSet {

	private final HashMap<String, AbstractCSSValue> overrideMap = new HashMap<String, AbstractCSSValue>();
	private final HashMap<String, Boolean> overridePrio = new HashMap<String, Boolean>();
	private final HashMap<String, AbstractCSSValue> nonOverrideMap = new HashMap<String, AbstractCSSValue>();
	private final HashMap<String, CSSShorthandValue> compatShorthandMap = new HashMap<String, CSSShorthandValue>();
	private final HashMap<String, CSSShorthandValue> nonOvShorthandMap = new HashMap<String, CSSShorthandValue>();

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

	void setCompatLonghand(String propertyName, AbstractCSSValue override, boolean priorityImportant,
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
			CSSShorthandValue shorthand = new CSSShorthandValue(value, important);
			shorthand.setShorthandText(cssText, cssText);
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
			short type = value.getLexicalUnitType();
			if (type == LexicalUnit2.SAC_COMPAT_IDENT || type == LexicalUnit2.SAC_COMPAT_PRIO) {
				return true;
			} else if (type == LexicalUnit.SAC_FUNCTION) {
				return containsIdentCompat(value.getParameters());
			} else if (type == LexicalUnit.SAC_SUB_EXPRESSION) {
				return containsIdentCompat(value.getSubValues());
			}
			value = value.getNextLexicalUnit();
		}
		return false;
	}

	static boolean isPriorityCompat(AbstractCSSValue compatvalue) {
		return compatvalue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((CSSPrimitiveValue) compatvalue).getPrimitiveType() == CSSPrimitiveValue.CSS_UNKNOWN
				&& ((CSSUnknownValue) compatvalue).isPriorityCompat();
	}

	static void appendIEPrioCharShorthandMinifiedCssText(StringBuilder sb, String shorthandName,
			CSSShorthandValue compatvalue) {
		sb.append(shorthandName).append(':').append(compatvalue.getMinifiedCssText(shorthandName));
		sb.append("!important!;");
	}

	static void writeIEPrioCharShorthandCssText(SimpleWriter wri, StyleFormattingContext context, String shorthandName,
			CSSShorthandValue compatvalue) throws IOException {
		context.startPropertyDeclaration(wri);
		wri.write(shorthandName);
		context.writeColon(wri);
		wri.write(compatvalue.getCssText());
		wri.write("!important!");
		context.writeSemiColon(wri);
		context.endPropertyDeclaration(wri);
	}

	static void writeIEPrioCharLonghandCssText(SimpleWriter wri, StyleFormattingContext context, String ptyname,
			AbstractCSSValue compatvalue) throws IOException {
		context.startPropertyDeclaration(wri);
		wri.write(ptyname);
		context.writeColon(wri);
		compatvalue.writeCssText(wri);
		wri.write("!important!");
		context.writeSemiColon(wri);
		context.endPropertyDeclaration(wri);
	}

	CSSShorthandValue getCompatShorthand(String shorthandName) {
		return compatShorthandMap.get(shorthandName);
	}

	boolean isCompatShorthand(String shorthandName) {
		return compatShorthandMap.containsKey(shorthandName);
	}

	boolean isCompatLonghand(String ptyname) {
		return overrideMap.containsKey(ptyname);
	}

	AbstractCSSValue getCompatLonghand(String ptyname) {
		return overrideMap.get(ptyname);
	}

	boolean isImportantCompatLonghand(String ptyname) {
		return overridePrio.get(ptyname);
	}

	CSSShorthandValue getNonOvCompatShorthand(String shorthandName) {
		return nonOvShorthandMap.get(shorthandName);
	}

	AbstractCSSValue getNonOvCompatLonghand(String ptyname) {
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
