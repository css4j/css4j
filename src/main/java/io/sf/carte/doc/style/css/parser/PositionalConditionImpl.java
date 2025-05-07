/*

 Copyright (c) 2005-2025, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.nsac.ElementSelector;
import io.sf.carte.doc.style.css.nsac.PositionalCondition;
import io.sf.carte.doc.style.css.nsac.Selector;
import io.sf.carte.doc.style.css.nsac.SelectorList;

class PositionalConditionImpl extends AbstractCondition implements PositionalCondition {

	private static final long serialVersionUID = 2L;

	int offset = 1; // By default, set to :first-child or :first-of-type
	int slope = 0;
	boolean forwardCondition = true;
	boolean oftype = false;
	private boolean hasArgument;
	boolean hasKeyword = false;
	SelectorList ofList = null;

	PositionalConditionImpl(boolean needsArgument) {
		super();
		this.hasArgument = needsArgument;
	}

	@Override
	public ConditionType getConditionType() {
		return Condition.ConditionType.POSITIONAL;
	}

	@Override
	public boolean isForwardCondition() {
		return forwardCondition;
	}

	@Override
	public boolean isOfType() {
		return oftype;
	}

	@Override
	public int getFactor() {
		return slope;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public boolean hasArgument() {
		return hasArgument;
	}

	/**
	 * The AnB expression is a keyword ?
	 * 
	 * @return <code>true</code> if the AnB expression is a keyword like
	 *         <code>odd</code>.
	 */
	@Override
	public boolean hasKeyword() {
		return hasKeyword;
	}

	@Override
	public SelectorList getOfList() {
		return ofList;
	}

	@Override
	Condition replace(SelectorList base) {
		PositionalConditionImpl clon = clone();
		if (ofList != null) {
			clon.ofList = ((SelectorListImpl) ofList).replaceNested(base);
		}
		return clon;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (forwardCondition ? 1231 : 1237);
		result = prime * result + (hasArgument ? 1231 : 1237);
		result = prime * result + (hasKeyword ? 1231 : 1237);
		result = prime * result + ((ofList == null) ? 0 : ofList.hashCode());
		result = prime * result + offset;
		result = prime * result + (oftype ? 1231 : 1237);
		result = prime * result + slope;
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
		PositionalConditionImpl other = (PositionalConditionImpl) obj;
		if (forwardCondition != other.forwardCondition) {
			return false;
		}
		if (hasArgument != other.hasArgument) {
			return false;
		}
		if (hasKeyword != other.hasKeyword) {
			return false;
		}
		if (ofList == null) {
			if (other.ofList != null) {
				return false;
			}
		} else if (!ParseHelper.equalSelectorList(ofList, other.ofList)) {
			return false;
		}
		if (offset != other.offset) {
			return false;
		}
		if (oftype != other.oftype) {
			return false;
		}
		if (slope != other.slope) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(':');
		if (oftype) {
			ofTypeSerialization(buf);
		} else {
			normalSerialization(buf);
		}
		return buf.toString();
	}

	private void normalSerialization(StringBuilder buf) {
		if (slope == 0) {
			if (offset == 1 && ofList == null && !hasArgument) {
				if (forwardCondition) {
					buf.append("first-child");
				} else {
					buf.append("last-child");
				}
			} else {
				if (forwardCondition) {
					buf.append("nth-child(");
				} else {
					buf.append("nth-last-child(");
				}
				buf.append(offset);
				if (!isUniversalOfList()) {
					buf.append(" of ").append(ofList.toString());
				}
				buf.append(')');
			}
		} else {
			if (forwardCondition) {
				buf.append("nth-child(");
			} else {
				buf.append("nth-last-child(");
			}
			appendAnB(buf);
			if (!isUniversalOfList()) {
				buf.append(" of ").append(ofList.toString());
			}
			buf.append(')');
		}
	}

	private void ofTypeSerialization(StringBuilder buf) {
		if (slope == 0) {
			if (offset == 1 && ofList == null && !hasArgument) {
				if (forwardCondition) {
					buf.append("first-of-type");
				} else {
					buf.append("last-of-type");
				}
			} else {
				if (forwardCondition) {
					buf.append("nth-of-type(");
				} else {
					buf.append("nth-last-of-type(");
				}
				buf.append(offset).append(')');
			}
		} else {
			if (forwardCondition) {
				buf.append("nth-of-type(");
			} else {
				buf.append("nth-last-of-type(");
			}
			appendAnB(buf);
			buf.append(')');
		}
	}

	private void appendAnB(StringBuilder buf) {
		if (hasKeyword && slope == 2) {
			if (offset == 0) {
				buf.append("even");
			} else {
				buf.append("odd");
			}
			return;
		}
		if (slope == -1) {
			buf.append('-');
		} else if (slope != 1) {
			buf.append(slope);
		}
		buf.append('n');
		if (offset > 0) {
			buf.append('+');
			buf.append(offset);
		} else if (offset != 0) {
			buf.append(offset);
		}
	}

	private boolean isUniversalOfList() {
		if (ofList == null) {
			return true;
		}
		for (int i = 0; i < ofList.getLength(); i++) {
			Selector sel = ofList.item(i);
			if (sel.getSelectorType() == Selector.SelectorType.UNIVERSAL
					&& ((ElementSelector) sel).getNamespaceURI() == null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public PositionalConditionImpl clone() {
		PositionalConditionImpl clon = (PositionalConditionImpl) super.clone();
		clon.forwardCondition = forwardCondition;
		clon.hasArgument = hasArgument;
		clon.hasKeyword = hasKeyword;
		clon.offset = offset;
		clon.oftype = oftype;
		clon.slope = slope;
		clon.ofList = ofList;
		return clon;
	}

}
