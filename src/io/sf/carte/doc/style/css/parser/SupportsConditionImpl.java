/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.Iterator;
import java.util.LinkedList;

import io.sf.carte.doc.style.css.SupportsCondition;

/**
 * CSS supports rule conditions implementation.
 * 
 * @author Carlos Amengual
 * 
 */
abstract class SupportsConditionImpl implements SupportsCondition {

	SupportsCondition parent = null;

	@Override
	public void setParentCondition(SupportsCondition parent) {
		this.parent = parent;
	}

	@Override
	public SupportsCondition getParentCondition() {
		return parent;
	}

	@Override
	abstract public ConditionType getType();

	@Override
	abstract public String getMinifiedText();

	@Override
	abstract public void addCondition(SupportsCondition nestedCondition);

	/**
	 * Replace the last condition added.
	 * 
	 * @param newCondition
	 *            the condition that replaces the last condition.
	 * 
	 * @return the replaced condition.
	 */
	@Override
	abstract public SupportsCondition replaceLast(SupportsCondition newCondition);

	static class NotCondition extends SupportsConditionImpl {
		SupportsCondition nestedCondition;

		NotCondition() {
			super();
		}

		@Override
		public void addCondition(SupportsCondition nestedCondition) {
			nestedCondition.setParentCondition(this);
			this.nestedCondition = nestedCondition;
		}

		@Override
		public SupportsCondition replaceLast(SupportsCondition newCondition) {
			SupportsCondition last = this.nestedCondition;
			addCondition(newCondition);
			return last;
		}

		@Override
		public ConditionType getType() {
			return ConditionType.NOT_CONDITION;
		}

		@Override
		public String getMinifiedText() {
			StringBuilder buf = new StringBuilder();
			boolean hasparent = getParentCondition() != null;
			if (hasparent) {
				buf.append('(');
			}
			buf.append("not");
			String nested = nestedCondition.getMinifiedText();
			if (nested.length() != 0 && nested.charAt(0) != '(') {
				buf.append(' ');
			}
			buf.append(nested);
			if (hasparent) {
				buf.append(')');
			}
			return buf.toString();
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			boolean hasparent = getParentCondition() != null;
			if (hasparent) {
				buf.append('(');
			}
			buf.append("not ").append(nestedCondition.toString());
			if (hasparent) {
				buf.append(')');
			}
			return buf.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((nestedCondition == null) ? 0 : nestedCondition.hashCode());
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
			NotCondition other = (NotCondition) obj;
			if (nestedCondition == null) {
				if (other.nestedCondition != null) {
					return false;
				}
			} else if (!nestedCondition.equals(other.nestedCondition)) {
				return false;
			}
			return true;
		}

	}

	static class AndCondition extends SupportsConditionImpl {
		LinkedList<SupportsCondition> nestedConditions;

		AndCondition() {
			super();
			nestedConditions = new LinkedList<SupportsCondition>();
		}

		@Override
		public void addCondition(SupportsCondition condition) {
			condition.setParentCondition(this);
			nestedConditions.add(condition);
		}

		@Override
		public SupportsCondition replaceLast(SupportsCondition newCondition) {
			SupportsCondition last = nestedConditions.removeLast();
			last.setParentCondition(null);
			addCondition(newCondition);
			return last;
		}

		@Override
		public ConditionType getType() {
			return ConditionType.AND_CONDITION;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((nestedConditions == null) ? 0 : nestedConditions.hashCode());
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
			AndCondition other = (AndCondition) obj;
			if (nestedConditions == null) {
				if (other.nestedConditions != null) {
					return false;
				}
			} else if (!nestedConditions.equals(other.nestedConditions)) {
				return false;
			}
			return true;
		}

		@Override
		public String getMinifiedText() {
			StringBuilder buf = new StringBuilder();
			boolean hasparent = getParentCondition() != null;
			if (hasparent) {
				buf.append('(');
			}
			Iterator<SupportsCondition> it = nestedConditions.iterator();
			buf.append(it.next().getMinifiedText());
			while (it.hasNext()) {
				if (buf.charAt(buf.length() - 1) != ')') {
					buf.append(' ');
				}
				buf.append("and");
				String nested = it.next().getMinifiedText();
				if (nested.length() != 0 && nested.charAt(0) != '(') {
					buf.append(' ');
				}
				buf.append(nested);
			}
			if (hasparent) {
				buf.append(')');
			}
			return buf.toString();
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			boolean hasparent = getParentCondition() != null;
			if (hasparent) {
				buf.append('(');
			}
			Iterator<SupportsCondition> it = nestedConditions.iterator();
			buf.append(it.next());
			while (it.hasNext()) {
				buf.append(" and ").append(it.next());
			}
			if (hasparent) {
				buf.append(')');
			}
			return buf.toString();
		}
	}

	static class OrCondition extends SupportsConditionImpl {
		LinkedList<SupportsCondition> nestedConditions;

		OrCondition() {
			super();
			nestedConditions = new LinkedList<SupportsCondition>();
		}

		@Override
		public void addCondition(SupportsCondition condition) {
			condition.setParentCondition(this);
			nestedConditions.add(condition);
		}

		@Override
		public SupportsCondition replaceLast(SupportsCondition newCondition) {
			SupportsCondition last = nestedConditions.removeLast();
			last.setParentCondition(null);
			addCondition(newCondition);
			return last;
		}

		@Override
		public ConditionType getType() {
			return ConditionType.OR_CONDITION;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((nestedConditions == null) ? 0 : nestedConditions.hashCode());
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
			OrCondition other = (OrCondition) obj;
			if (nestedConditions == null) {
				if (other.nestedConditions != null) {
					return false;
				}
			} else if (!nestedConditions.equals(other.nestedConditions)) {
				return false;
			}
			return true;
		}

		@Override
		public String getMinifiedText() {
			StringBuilder buf = new StringBuilder();
			boolean hasparent = getParentCondition() != null;
			if (hasparent) {
				buf.append('(');
			}
			Iterator<SupportsCondition> it = nestedConditions.iterator();
			buf.append(it.next().getMinifiedText());
			while (it.hasNext()) {
				if (buf.charAt(buf.length() - 1) != ')') {
					buf.append(' ');
				}
				buf.append("or");
				String nested = it.next().getMinifiedText();
				if (nested.length() != 0 && nested.charAt(0) != '(') {
					buf.append(' ');
				}
				buf.append(nested);
			}
			if (hasparent) {
				buf.append(')');
			}
			return buf.toString();
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			boolean hasparent = getParentCondition() != null;
			if (hasparent) {
				buf.append('(');
			}
			Iterator<SupportsCondition> it = nestedConditions.iterator();
			buf.append(it.next());
			while (it.hasNext()) {
				buf.append(" or ").append(it.next().toString());
			}
			if (hasparent) {
				buf.append(')');
			}
			return buf.toString();
		}
	}

}
