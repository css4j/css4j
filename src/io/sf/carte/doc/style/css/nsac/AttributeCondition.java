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
 * Based on SAC's <code>AttributeCondition</code> interface by Philippe Le Hegaret.
 */
public interface AttributeCondition extends Condition {

	/**
	 * Returns the <a href="http://www.w3.org/TR/REC-xml-names/#NT-LocalPart">local part</a>
	 * of the <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">qualified name</a> of
	 * this attribute.
	 * <p>
	 * If this condition represents a pseudo-class, it returns the pseudo-class name.
	 *
	 * @return the local name of the attribute specified by this condition, or
	 *         <code>null</code> if :
	 *         <ul>
	 *         <li>
	 *         <p>
	 *         this attribute condition can match any attribute.
	 *         <li>
	 *         <p>
	 *         this attribute is a class attribute.
	 *         <li>
	 *         <p>
	 *         this attribute is an id attribute.
	 *         </ul>
	 */
	String getLocalName();

    /**
     * Returns the
     * <a href="http://www.w3.org/TR/REC-xml-names/#dt-NSName">namespace
     * URI</a> of this attribute condition.
     * <p><code>NULL</code> if :
     * <ul>
     * <li>this attribute condition can match any namespace.
     * <li>this attribute is an id attribute.
     * </ul>
     */    
    String getNamespaceURI();

	/**
	 * If this attribute is a class attribute, you'll get the class name without the
	 * '.'.
	 * <p>
	 * If this condition represents a pseudo-class with an argument (in
	 * parentheses), it returns the argument.
	 *
	 * @return the value of the attribute (or the pseudo-class argument).
	 */
	String getValue();

	/**
	 * Attribute selector flags.
	 */
	public enum Flag {
		/**
		 * Case insensitive comparisons should be used.
		 */
		CASE_I,

		/**
		 * Case sensitive comparisons should be used.
		 */
		CASE_S
	}

	/**
	 * Test this selector for the given flag.
	 *
	 * @param flag
	 *            the flag to test.
	 * @return <code>true</code> if the supplied flag was set for this condition.
	 */
	boolean hasFlag(Flag flag);

}
