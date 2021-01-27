/*

 Copyright (c) 2005-2021, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

/**
 * NOT condition.
 * 
 */
class NotCondition extends BooleanConditionImpl.NotCondition {

	NotCondition() {
		super();
	}

	@Override
	public void appendMinifiedText(StringBuilder buf) {
		int buflen = buf.length();
		boolean hasparent = getParentCondition() != null;
		if (hasparent) {
			buf.append("(not ");
		} else {
			if (buflen != 0) {
				buf.append(" not ");
			} else {
				buf.append("not ");
			}
		}
		nestedCondition.appendMinifiedText(buf);
		if (hasparent) {
			buf.append(')');
		}
	}

	@Override
	public void appendText(StringBuilder buf) {
		int buflen = buf.length();
		boolean hasparent = getParentCondition() != null;
		if (hasparent) {
			buf.append("(not ");
		} else {
			if (buflen != 0) {
				buf.append(" not ");
			} else {
				buf.append("not ");
			}
		}
		nestedCondition.appendText(buf);
		if (hasparent) {
			buf.append(')');
		}
	}

}
