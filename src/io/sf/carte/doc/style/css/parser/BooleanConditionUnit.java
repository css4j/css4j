/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * LexicalUnit-based boolean conditions.
 * <p>
 * Used by media queries and <code>{@literal @}supports</code> conditions.
 * </p>
 * 
 * @author Carlos Amengual
 * 
 */
abstract class BooleanConditionUnit extends LexicalUnitImpl implements BooleanCondition {

	private BooleanCondition parent = null;

	BooleanConditionUnit(LexicalType unitType) {
		super(unitType);
	}

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
	String currentToString() {
		return getCssText();
	}

	@Override
	public String getCssText() {
		StringBuilder buf = new StringBuilder(32);
		appendText(buf);
		return buf.toString();
	}

	static abstract class GroupCondition extends BooleanConditionUnit {
		LinkedList<BooleanCondition> nestedConditions;

		GroupCondition(LexicalType unitType) {
			super(unitType);
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

	static class AndCondition extends GroupCondition {

		AndCondition() {
			super(LexicalType.CONDITION_AND);
			nestedConditions = new LinkedList<BooleanCondition>();
		}

		@Override
		public Type getType() {
			return Type.AND;
		}

		@Override
		public void appendText(StringBuilder buf) {
			BooleanConditionHelper.appendANDText(this, buf);
		}

		@Override
		public void appendMinifiedText(StringBuilder buf) {
			BooleanConditionHelper.appendANDMinifiedText(this, buf);
		}

		@Override
		public int hashCode() {
			return super.hashCode() * 31 + 13;
		}

	}

	static class OrCondition extends GroupCondition {

		OrCondition() {
			super(LexicalType.CONDITION_OR);
		}

		@Override
		public Type getType() {
			return Type.OR;
		}

		@Override
		public int hashCode() {
			return super.hashCode() * 31 + 7;
		}

		@Override
		public void appendText(StringBuilder buf) {
			BooleanConditionHelper.appendORText(this, buf);
		}

		@Override
		public void appendMinifiedText(StringBuilder buf) {
			BooleanConditionHelper.appendORMinifiedText(this, buf);
		}

	}

	static class NotCondition extends BooleanConditionUnit {
		BooleanCondition nestedCondition;

		NotCondition() {
			super(LexicalType.CONDITION_NOT);
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
		public void appendText(StringBuilder buf) {
			BooleanConditionHelper.appendNOTText(this, buf);
		}

		@Override
		public void appendMinifiedText(StringBuilder buf) {
			BooleanConditionHelper.appendNOTMinifiedText(this, buf);
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

	static class Predicate extends BooleanConditionUnit {

		private final String name;

		Predicate(String name) {
			super(LexicalType.CONDITION_PREDICATE);
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
		public void appendText(StringBuilder buf) {
			buf.append(name);
		}

		@Override
		public void appendMinifiedText(StringBuilder buf) {
			buf.append(name);
		}

		@Override
		public String getCssText() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(name);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
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
