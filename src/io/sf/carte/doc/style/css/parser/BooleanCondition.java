/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.parser;

import java.util.List;

/**
 * Boolean conditions.
 * <p>
 * Used by media queries and <code>{@literal @}supports</code> conditions.
 * 
 * @author Carlos Amengual
 * 
 */
public interface BooleanCondition {

	/**
	 * The types of condition. A <code>PREDICATE</code> is the fundamental condition
	 * that is carried by the other condition types (<code>AND</code>,
	 * <code>OR</code>, <code>NOT</code>).
	 */
	public enum Type {
		PREDICATE, OR, AND, NOT
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
	 * On a <code>NOT</code> condition this adds the negated condition, for grouping
	 * conditions (<code>AND</code>, <code>OR</code>), adds a condition to the set
	 * of sub-conditions.
	 * <p>
	 * On a <code>PREDICATE</code>, should either do nothing or throw a runtime
	 * exception (behaviour is implementation-dependent).
	 * 
	 * @param subCondition
	 *            the sub-condition.
	 */
	void addCondition(BooleanCondition subCondition);

	/**
	 * Replace the last condition added.
	 * 
	 * @param newCondition
	 *            the condition that replaces the last condition.
	 * 
	 * @return the replaced condition.
	 */
	BooleanCondition replaceLast(BooleanCondition newCondition);

	/**
	 * Append a serialization of the condition.
	 * 
	 * @param buf the buffer to append to.
	 */
	void appendText(StringBuilder buf);

	/**
	 * Append a minified serialization of the condition.
	 * 
	 * @param buf the buffer to append to.
	 */
	void appendMinifiedText(StringBuilder buf);

}
