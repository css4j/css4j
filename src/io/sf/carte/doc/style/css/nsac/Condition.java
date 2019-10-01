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

	/**
	 * This condition checks two conditions in a compound selector. Example:
	 * 
	 * <pre class="example">
	 *   .part1:lang(fr)
	 * </pre>
	 * 
	 * @see CombinatorCondition
	 */
	short SAC_AND_CONDITION = 0;

	/**
	 * This condition checks a specified position. Example:
	 * 
	 * <pre class="example">
	 *   :first-child
	 * </pre>
	 * 
	 * @see PositionalCondition
	 */
	short SAC_POSITIONAL_CONDITION = 3;

	/**
	 * This condition checks for the presence of an attribute (and eventually its value). Example:
	 * 
	 * <pre class="example">
	 *   [simple]
	 *   [restart="never"]
	 * </pre>
	 * 
	 * @see AttributeCondition
	 */
	short SAC_ATTRIBUTE_CONDITION = 4;

	/**
	 * This condition checks an id attribute. Example:
	 * 
	 * <pre class="example">
	 *   #myId
	 * </pre>
	 * 
	 * @see AttributeCondition
	 */
	short SAC_ID_CONDITION = 5;

	/**
	 * This condition checks the language of the node. Example:
	 * 
	 * <pre class="example">
	 *   :lang(fr)
	 * </pre>
	 * 
	 * @see LangCondition
	 */
	short SAC_LANG_CONDITION = 6;

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
	short SAC_ONE_OF_ATTRIBUTE_CONDITION = 7;

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
	short SAC_BEGIN_HYPHEN_ATTRIBUTE_CONDITION = 8;

	/**
	 * This condition checks for a specified class. Example:
	 * 
	 * <pre class="example">
	 *   .example
	 * </pre>
	 * 
	 * @see AttributeCondition
	 */
	short SAC_CLASS_CONDITION = 9;

	/**
	 * This condition checks for a pseudo class. Example:
	 * 
	 * <pre class="example">
	 *   :link
	 *   :visited
	 *   :hover
	 * </pre>
	 * 
	 * @see AttributeCondition
	 */
	short SAC_PSEUDO_CLASS_CONDITION = 10;

	/**
	 * This condition checks if a node is the only one in the node list.
	 */
	short SAC_ONLY_CHILD_CONDITION = 11;

	/**
	 * This condition checks if a node is the only one of his type.
	 */
	short SAC_ONLY_TYPE_CONDITION = 12;

	/**
	 * This condition checks the beginning of an attribute value. Example:
	 *
	 * <pre class="example">
	 *   [restart^="never"]
	 * </pre>
	 *
	 * @see org.w3c.css.sac.AttributeCondition
	 */
	short SAC_BEGINS_ATTRIBUTE_CONDITION = 14;

	/**
	 * This condition checks the end of an attribute value. Example:
	 *
	 * <pre class="example">
	 *   [restart$="never"]
	 * </pre>
	 *
	 * @see org.w3c.css.sac.AttributeCondition
	 */
	short SAC_ENDS_ATTRIBUTE_CONDITION = 15;

	/**
	 * This condition checks a substring of an attribute value. Example:
	 *
	 * <pre class="example">
	 *   [restart*="never"]
	 * </pre>
	 *
	 * @see org.w3c.css.sac.AttributeCondition
	 */
	short SAC_SUBSTRING_ATTRIBUTE_CONDITION = 16;

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
	short SAC_SELECTOR_ARGUMENT_CONDITION = 17;

	/**
	 * This condition checks for pseudo elements. Example:
	 *
	 * <pre class="example">
	 *   ::first-line
	 *   ::first-letter
	 * </pre>
	 *
	 * @see org.w3c.css.sac.AttributeCondition#getLocalName()
	 */
	short SAC_PSEUDO_ELEMENT_CONDITION = 18;

	/**
	 * Get the type of <code>Condition</code>.
	 * 
	 * @return the type of condition.
	 */
	short getConditionType();

}
