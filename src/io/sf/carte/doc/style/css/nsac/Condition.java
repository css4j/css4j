/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * Copyright © 2017-2019 Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

/**
 * Based on SAC's {@code Condition} interface by Philippe Le Hegaret.
 */
public interface Condition {

	enum ConditionType {
		/**
		 * This condition checks two conditions in a compound selector. Example:
		 * 
		 * <pre class="example">
		 *   .part1:lang(fr)
		 * </pre>
		 * 
		 * @see CombinatorCondition
		 */
		AND,

		/**
		 * This condition checks an id attribute. Example:
		 * 
		 * <pre class="example">
		 *   #myId
		 * </pre>
		 * 
		 * @see AttributeCondition
		 */
		ID,

		/**
		 * This condition checks for a specified class. Example:
		 * 
		 * <pre class="example">
		 *   .example
		 * </pre>
		 * 
		 * @see AttributeCondition
		 */
		CLASS,

		/**
		 * This condition checks for a pseudo class. Example:
		 * 
		 * <pre class="example">
		 *   :link
		 *   :visited
		 *   :hover
		 * </pre>
		 * 
		 * @see PseudoCondition
		 */
		PSEUDO_CLASS,

		/**
		 * This condition checks for pseudo elements. Example:
		 *
		 * <pre class="example">
		 *   ::first-line
		 *   ::first-letter
		 * </pre>
		 *
		 * @see PseudoCondition#getName()
		 */
		PSEUDO_ELEMENT,

		/**
		 * This condition checks a specified position. Example:
		 * 
		 * <pre class="example">
		 *   :first-child
		 * </pre>
		 * 
		 * @see PositionalCondition
		 */
		POSITIONAL,

		/**
		 * This condition checks if a node is the only one in the node list.
		 */
		ONLY_CHILD,

		/**
		 * This condition checks if a node is the only one of his type.
		 */
		ONLY_TYPE,

		/**
		 * This condition checks the language of the node. Example:
		 * 
		 * <pre class="example">
		 *   :lang(fr)
		 * </pre>
		 * 
		 * @see LangCondition
		 */
		LANG,

		/**
		 * This condition checks for the presence of an attribute (and eventually its
		 * value). Example:
		 * 
		 * <pre class="example">
		 *   [simple]
		 *   [restart="never"]
		 * </pre>
		 * 
		 * @see AttributeCondition
		 */
		ATTRIBUTE,

		/**
		 * This condition checks for a value in a list of space-separated values in an
		 * attribute. Example:
		 * 
		 * <pre class="example">
		 *   [values~="10"]
		 * </pre>
		 * 
		 * @see AttributeCondition
		 */
		ONE_OF_ATTRIBUTE,

		/**
		 * This condition checks if the value is in a hypen-separated list of values in
		 * a specified attribute. Example:
		 * 
		 * <pre class="example">
		 *   [languages|="fr"]
		 * </pre>
		 * 
		 * @see AttributeCondition
		 */
		BEGIN_HYPHEN_ATTRIBUTE,

		/**
		 * This condition checks the beginning of an attribute value. Example:
		 *
		 * <pre class="example">
		 *   [restart^="never"]
		 * </pre>
		 *
		 * @see AttributeCondition
		 */
		BEGINS_ATTRIBUTE,

		/**
		 * This condition checks the end of an attribute value. Example:
		 *
		 * <pre class="example">
		 *   [restart$="never"]
		 * </pre>
		 *
		 * @see AttributeCondition
		 */
		ENDS_ATTRIBUTE,

		/**
		 * This condition checks a substring of an attribute value. Example:
		 *
		 * <pre class="example">
		 *   [restart*="never"]
		 * </pre>
		 *
		 * @see AttributeCondition
		 */
		SUBSTRING_ATTRIBUTE,

		/**
		 * This condition checks the selector list argument to which a pseudo-class
		 * applies. Example:
		 *
		 * <pre class="example">
		 *   :not(:visited,:hover)
		 * </pre>
		 *
		 * @see ArgumentCondition
		 */
		SELECTOR_ARGUMENT
	}

	/**
	 * Get the type of <code>Condition</code>.
	 * 
	 * @return the type of condition.
	 */
	ConditionType getConditionType();

}
