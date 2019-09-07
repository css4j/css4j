/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

/**
 * CSS supports rule conditions.
 *
 * @author Carlos Amengual
 *
 */
public interface SupportsCondition {

	public enum ConditionType {
		DECLARATION_CONDITION, OR_CONDITION, AND_CONDITION, NOT_CONDITION
	}

	/**
	 * The type of condition.
	 *
	 * @return the condition type.
	 */
	ConditionType getType();

	/**
	 * Get the parent condition, if any.
	 *
	 * @return the parent condition, or <code>null</code> if none.
	 */
	SupportsCondition getParentCondition();

	/**
	 * Set the parent condition.
	 *
	 * @param parent
	 *            the parent condition.
	 */
	void setParentCondition(SupportsCondition parent);

	/**
	 * Add a condition to a boolean condition.
	 *
	 * @param nestedCondition
	 *            the nested condition.
	 */
	void addCondition(SupportsCondition nestedCondition);

	/**
	 * Replace the last condition added.
	 *
	 * @param newCondition
	 *            the condition that replaces the last condition.
	 *
	 * @return the replaced condition.
	 */
	SupportsCondition replaceLast(SupportsCondition newCondition);

	/**
	 * Get a minified serialization of the condition.
	 *
	 * @return the minified serialization of this condition.
	 */
	String getMinifiedText();

}
