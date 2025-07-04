/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.om;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.sf.carte.doc.style.css.BooleanCondition;

/**
 * Base implementation for boolean conditions.
 * <p>
 * Used by media queries and <code>{@literal @}supports</code> conditions.
 * 
 */
abstract class BooleanConditionImpl implements BooleanCondition, java.io.Serializable {

	private static final long serialVersionUID = 1L;

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

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(64);
		appendText(buf);
		return buf.toString();
	}

	static abstract class GroupCondition extends BooleanConditionImpl {

		private static final long serialVersionUID = 1L;

		final LinkedList<BooleanCondition> nestedConditions;

		GroupCondition() {
			super();
			nestedConditions = new LinkedList<>();
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

	static abstract class Predicate extends BooleanConditionImpl {

		private static final long serialVersionUID = 2L;

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

		/**
		 * An implementation-dependent number indicative of the predicate type.
		 * <p>
		 * The default implementation returns {@code 0}.
		 * </p>
		 * 
		 * @return the predicate type.
		 */
		public int getPredicateType() {
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
