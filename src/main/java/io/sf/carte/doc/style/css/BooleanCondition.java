/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css;

import java.util.List;

/**
 * Boolean conditions.
 * <p>
 * Used by media queries and <code>{@literal @}supports</code> conditions.
 * 
 */
public interface BooleanCondition {

	/**
	 * The types of condition. A <code>PREDICATE</code> is the fundamental condition
	 * that is carried by the other condition types (<code>AND</code>,
	 * <code>OR</code>, <code>NOT</code>).
	 */
	enum Type {
		/**
		 * An implementation-dependent predicate.
		 */
		PREDICATE,

		/**
		 * A supported selector function predicate.
		 * 
		 * Cast to {@link io.sf.carte.doc.style.css.nsac.SelectorFunction
		 * SelectorFunction}.
		 */
		SELECTOR_FUNCTION,

		/**
		 * An {@code OR} condition.
		 * <p>
		 * Use {@link BooleanCondition#getSubConditions()}.
		 * </p>
		 */
		OR,

		/**
		 * An {@code AND} condition.
		 * <p>
		 * Use {@link BooleanCondition#getSubConditions()}.
		 * </p>
		 */
		AND,

		/**
		 * A {@code NOT} condition.
		 * <p>
		 * Use {@link BooleanCondition#getNestedCondition()}.
		 * </p>
		 */
		NOT,

		/**
		 * Other, unknown or unsupported condition types.
		 */
		OTHER
	}

	/**
	 * The type of condition.
	 * 
	 * @return the condition type.
	 */
	Type getType();

	/**
	 * If this condition is composed by a set of conditions (forming an
	 * <code>AND</code> or <code>OR</code> expression), return it.
	 * <p>
	 * The set of sub-conditions is returned as a list in specified order.
	 * 
	 * @return the list of sub-conditions, or <code>null</code> if this condition
	 *         contains no sub-conditions (i.e. <code>NOT</code> or
	 *         <code>PREDICATE</code>).
	 */
	List<BooleanCondition> getSubConditions();

	/**
	 * If this is a <code>NOT</code> condition, return the negated condition.
	 * 
	 * @return the negated condition, or <code>null</code> if this condition is not
	 *         a <code>NOT</code> condition, or contains no negated condition.
	 */
	BooleanCondition getNestedCondition();

	/**
	 * Get the parent condition, if any.
	 * 
	 * @return the parent condition, or <code>null</code> if none.
	 */
	BooleanCondition getParentCondition();

	/**
	 * Set the parent condition, if this condition is nested into another.
	 * 
	 * @param parent
	 *            the parent condition.
	 */
	void setParentCondition(BooleanCondition parent);

	/**
	 * Add a condition to a boolean condition.
	 * <p>
	 * On a <code>NOT</code> condition this sets the negated condition, for grouping
	 * conditions (<code>AND</code>, <code>OR</code>), adds a condition to the set
	 * of sub-conditions.
	 * <p>
	 * On a <code>PREDICATE</code>, should either do nothing or throw a runtime
	 * exception (behaviour is implementation-dependent).
	 * 
	 * @param subCondition
	 *            the sub-condition (or the negated condition).
	 */
	void addCondition(BooleanCondition subCondition);

	/**
	 * Replace the last nested/sub condition added.
	 * 
	 * @param newCondition
	 *            the condition that replaces the last condition.
	 * 
	 * @return the replaced condition.
	 */
	BooleanCondition replaceLast(BooleanCondition newCondition);

	/**
	 * Append a serialization of the condition to the given buffer.
	 * 
	 * @param buf the buffer to append to.
	 */
	void appendText(StringBuilder buf);

	/**
	 * Append a minified serialization of the condition to the given buffer.
	 * 
	 * @param buf the buffer to append to.
	 */
	default void appendMinifiedText(StringBuilder buf) {
		appendText(buf);
	}

}
