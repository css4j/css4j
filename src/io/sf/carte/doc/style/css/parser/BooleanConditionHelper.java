/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Iterator;

/**
 * Helper class for {@code BooleanCondition}.
 * 
 */
public class BooleanConditionHelper {

	public static void appendANDMinifiedText(BooleanCondition condition, StringBuilder buf) {
		boolean hasparent = condition.getParentCondition() != null;
		if (hasparent) {
			buf.append('(');
		}
		Iterator<BooleanCondition> it = condition.getSubConditions().iterator();
		it.next().appendMinifiedText(buf);
		while (it.hasNext()) {
			buf.append(" and ");
			it.next().appendMinifiedText(buf);
		}
		if (hasparent) {
			buf.append(')');
		}
	}

	public static void appendANDText(BooleanCondition condition, StringBuilder buf) {
		boolean hasparent = condition.getParentCondition() != null;
		if (hasparent) {
			buf.append('(');
		}
		Iterator<BooleanCondition> it = condition.getSubConditions().iterator();
		it.next().appendText(buf);
		while (it.hasNext()) {
			buf.append(" and ");
			it.next().appendText(buf);
		}
		if (hasparent) {
			buf.append(')');
		}
	}

	public static void appendORMinifiedText(BooleanCondition condition, StringBuilder buf) {
		boolean hasparent = condition.getParentCondition() != null;
		if (hasparent) {
			buf.append('(');
		}
		Iterator<BooleanCondition> it = condition.getSubConditions().iterator();
		it.next().appendMinifiedText(buf);
		while (it.hasNext()) {
			buf.append(" or ");
			it.next().appendMinifiedText(buf);
		}
		if (hasparent) {
			buf.append(')');
		}
	}

	public static void appendORText(BooleanCondition condition, StringBuilder buf) {
		boolean hasparent = condition.getParentCondition() != null;
		if (hasparent) {
			buf.append('(');
		}
		Iterator<BooleanCondition> it = condition.getSubConditions().iterator();
		it.next().appendText(buf);
		while (it.hasNext()) {
			buf.append(" or ");
			it.next().appendText(buf);
		}
		if (hasparent) {
			buf.append(')');
		}
	}

	public static void appendNOTMinifiedText(BooleanCondition condition, StringBuilder buf) {
		int buflen = buf.length();
		boolean hasparent = condition.getParentCondition() != null;
		if (hasparent) {
			buf.append("(not ");
		} else {
			if (buflen != 0) {
				buf.append(" not ");
			} else {
				buf.append("not ");
			}
		}
		condition.getNestedCondition().appendMinifiedText(buf);
		if (hasparent) {
			buf.append(')');
		}
	}

	public static void appendNOTText(BooleanCondition condition, StringBuilder buf) {
		int buflen = buf.length();
		boolean hasparent = condition.getParentCondition() != null;
		if (hasparent) {
			buf.append("(not ");
		} else {
			if (buflen != 0) {
				buf.append(" not ");
			} else {
				buf.append("not ");
			}
		}
		condition.getNestedCondition().appendText(buf);
		if (hasparent) {
			buf.append(')');
		}
	}

}
