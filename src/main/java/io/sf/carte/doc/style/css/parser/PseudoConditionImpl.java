/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.parser;

import java.util.StringTokenizer;

import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.PseudoCondition;

class PseudoConditionImpl extends AbstractNamedCondition implements PseudoCondition {

	private static final long serialVersionUID = 1L;

	private ConditionType condType;

	String argument = null;

	PseudoConditionImpl(ConditionType condType) {
		super();
		this.condType = condType;
	}

	@Override
	public ConditionType getConditionType() {
		return condType;
	}

	@Override
	public String getArgument() {
		return argument;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + condType.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((argument == null) ? 0 : argument.hashCode());
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
		PseudoConditionImpl other = (PseudoConditionImpl) obj;
		if (condType != other.condType) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (argument == null) {
			if (other.argument != null) {
				return false;
			}
		} else if (!argument.equals(other.argument)) {
			return false;
		}
		return true;
	}

	@Override
	void serialize(StringBuilder buf) {
		if (condType == Condition.ConditionType.PSEUDO_CLASS) {
			buf.append(':');
		} else {
			buf.append("::");
		}
		buf.append(NSACSelectorFactory.escapeName(name));
		if (argument != null) {
			buf.append('(');
			if (isSelectorArgument()) {
				buf.append(argument);
			} else if (argument.isEmpty()) {
				buf.append("''");
			} else if (argument.indexOf(' ') != -1) {
				StringTokenizer st = new StringTokenizer(argument, " ");
				String ident = st.nextToken();
				buf.append(ParseHelper.escape(ident));
				while (st.hasMoreElements()) {
					ident = st.nextToken();
					buf.append(' ');
					buf.append(ParseHelper.escape(ident));
				}
			} else {
				buf.append(ParseHelper.escape(argument));
			}
			buf.append(')');
		}
	}

	private boolean isSelectorArgument() {
		int len = argument.length();
		for (int i = 0; i < len; i++) {
			char c = argument.charAt(i);
			if (c == '*' || c == '.') {
				return true;
			}
		}
		return false;
	}

	@Override
	public PseudoConditionImpl clone() {
		PseudoConditionImpl clon = (PseudoConditionImpl) super.clone();
		clon.condType = condType;
		clon.argument = argument;
		return clon;
	}

}
