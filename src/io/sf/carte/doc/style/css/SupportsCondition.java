/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.agent.CSSCanvas;

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
	public ConditionType getType();

	/**
	 * Get the parent condition, if any.
	 * 
	 * @return the parent condition, or <code>null</code> if none.
	 */
	public SupportsCondition getParentCondition();

	/**
	 * Set the parent condition.
	 * 
	 * @param parent
	 *            the parent condition.
	 */
	public void setParentCondition(SupportsCondition parent);

	/**
	 * Add a condition to a boolean condition.
	 * 
	 * @param nestedCondition
	 *            the nested condition.
	 */
	public void addCondition(SupportsCondition nestedCondition);

	/**
	 * Replace the last condition added.
	 * 
	 * @param newCondition
	 *            the condition that replaces the last condition.
	 * 
	 * @return the replaced condition.
	 */
	public SupportsCondition replaceLast(SupportsCondition newCondition);

	/**
	 * Get a minified serialization of the condition.
	 * 
	 * @return the minified serialization of this condition.
	 */
	public String getMinifiedText();

	/**
	 * Check whether the given canvas supports this condition.
	 * 
	 * @param canvas
	 *            the canvas.
	 * @return <code>true</code> if the canvas supports this condition.
	 */
	public boolean supports(CSSCanvas canvas);

	/**
	 * A condition that declares a feature name and a value.
	 *
	 * @param <T>
	 *            the value type.
	 */
	public interface DeclarationCondition<T extends ExtendedCSSValue> extends SupportsCondition {
		/**
		 * Set the condition feature value.
		 * 
		 * @param value
		 *            the value.
		 * @throws DOMException
		 *             if the value is incompatible with the feature being tested with the
		 *             condition.
		 */
		public void setValue(T value) throws DOMException;
	}

}
