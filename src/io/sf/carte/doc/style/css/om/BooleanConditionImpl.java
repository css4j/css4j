/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.sf.carte.doc.style.css.parser.BooleanCondition;

/**
 * Base implementation for boolean conditions.
 * <p>
 * Used by media queries and <code>{@literal @}supports</code> conditions.
 * 
 * @author Carlos Amengual
 * 
 */
abstract class BooleanConditionImpl implements BooleanCondition {

	private BooleanCondition parent = null;

	/**
	 * If this condition is composed of a list of conditions, return them.
	 * 
	 * @return the list of sub-conditions, or <code>null</code> if this condition
	 *         contains no sub-conditions.
	 */
	@Override
	public List<BooleanCondition> getSubConditions() {
		return null;
	}

	/**
	 * If this is a <code>NOT</code> condition, return the negated condition.
	 * 
	 * @return the negated condition, or <code>null</code> if this condition is not
	 *         a <code>NOT</code> condition, or contains no negated condition.
	 */
	@Override
	public BooleanCondition getNestedCondition() {
		return null;
	}

	/**
	 * Get the parent condition, if any.
	 * 
	 * @return the parent condition, or <code>null</code> if none.
	 */
	@Override
	public BooleanCondition getParentCondition() {
		return parent;
	}

	/**
	 * Set the parent condition.
	 * 
	 * @param parent
	 *            the parent condition.
	 */
	@Override
	public void setParentCondition(BooleanCondition parent) {
		this.parent = parent;
	}

	/**
	 * Add a condition to a boolean condition.
	 * 
	 * @param nestedCondition
	 *            the nested condition.
	 */
	@Override
	abstract public void addCondition(BooleanCondition nestedCondition);

	/**
	 * Replace the last condition added.
	 * 
	 * @param newCondition
	 *            the condition that replaces the last condition.
	 * 
	 * @return the replaced condition.
	 */
	@Override
	abstract public BooleanCondition replaceLast(BooleanCondition newCondition);

	/**
	 * Append a serialization of the condition.
	 * 
	 * @param buf the buffer to append to.
	 */
	@Override
	abstract public void appendText(StringBuilder buf);

	/**
	 * Append a minified serialization of the condition.
	 * 
	 * @param buf the buffer to append to.
	 */
	@Override
	abstract public void appendMinifiedText(StringBuilder buf);

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		appendText(buf);
		return buf.toString();
	}

	static abstract class GroupCondition extends BooleanConditionImpl {
		final LinkedList<BooleanCondition> nestedConditions;

		GroupCondition() {
			super();
			nestedConditions = new LinkedList<BooleanCondition>();
		}

		@Override
		public void addCondition(BooleanCondition condition) {
			condition.setParentCondition(this);
			nestedConditions.add(condition);
		}

		@Override
		public BooleanCondition replaceLast(BooleanCondition newCondition) {
			BooleanCondition last = nestedConditions.removeLast();
			last.setParentCondition(null);
			addCondition(newCondition);
			return last;
		}

		@Override
		public List<BooleanCondition> getSubConditions() {
			return nestedConditions;
		}

		@Override
		public int hashCode() {
			return ((nestedConditions == null) ? 0 : nestedConditions.hashCode());
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
			GroupCondition other = (GroupCondition) obj;
			if (nestedConditions == null) {
				if (other.nestedConditions != null) {
					return false;
				}
			} else if (!nestedConditions.equals(other.nestedConditions)) {
				return false;
			}
			return true;
		}

	}

	static abstract class AndCondition extends GroupCondition {

		AndCondition() {
			super();
		}

		@Override
		public Type getType() {
			return Type.AND;
		}

		@Override
		public int hashCode() {
			return super.hashCode() * 31 + 13;
		}

	}

	static abstract class OrCondition extends GroupCondition {

		OrCondition() {
			super();
		}

		@Override
		public Type getType() {
			return Type.OR;
		}

		@Override
		public int hashCode() {
			return super.hashCode() * 31 + 7;
		}

	}

	static abstract class NotCondition extends BooleanConditionImpl {
		BooleanCondition nestedCondition;

		NotCondition() {
			super();
		}

		@Override
		public void addCondition(BooleanCondition nestedCondition) {
			nestedCondition.setParentCondition(this);
			this.nestedCondition = nestedCondition;
		}

		@Override
		public BooleanCondition replaceLast(BooleanCondition newCondition) {
			BooleanCondition last = this.nestedCondition;
			addCondition(newCondition);
			return last;
		}

		@Override
		public Type getType() {
			return Type.NOT;
		}

		@Override
		public BooleanCondition getNestedCondition() {
			return nestedCondition;
		}

		@Override
		public int hashCode() {
			return ((nestedCondition == null) ? 0 : nestedCondition.hashCode());
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

	static abstract class Predicate extends BooleanConditionImpl {

		private final String name;

		protected Predicate(String name) {
			super();
			this.name = name;
		}

		/**
		 * Get a name that identifies what this predicate is about, like the name of a
		 * media feature or property.
		 * 
		 * @return a name representative of this predicate.
		 */
		public String getName() {
			return name;
		}

		@Override
		public Type getType() {
			return Type.PREDICATE;
		}

		public short getPredicateType() {
			return 0;
		}

		@Override
		public void addCondition(BooleanCondition nestedCondition) {
		}

		@Override
		public BooleanCondition replaceLast(BooleanCondition newCondition) {
			return this;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
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
			Predicate other = (Predicate) obj;
			return Objects.equals(name, other.name);
		}

	}

}
