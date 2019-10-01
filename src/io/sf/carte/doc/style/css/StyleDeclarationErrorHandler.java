/*

 Copyright (c) 2005-2019, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.nsac.CSSParseException;
import io.sf.carte.doc.style.css.nsac.LexicalUnit;
import io.sf.carte.doc.style.css.property.CSSPropertyValueException;

/**
 * Handles the errors found in style declarations.
 * <p>
 * The reported errors can be logged, ignored, or used by some tool.
 * </p>
 * 
 * @author Carlos Amengual
 *
 */
public interface StyleDeclarationErrorHandler {

	/**
	 * An URI CSS value contained a malformed URI.
	 * 
	 * @param uri
	 *            the malformed uri.
	 */
	void malformedURIValue(String uri);

	/**
	 * When processing a shorthand, a syntax error was found.
	 * 
	 * @param shorthandName
	 *            the name of the shorthand property being processed.
	 * @param message
	 *            a message describing the error.
	 */
	void shorthandSyntaxError(String shorthandName, String message);

	/**
	 * A shorthand could be processed, but was found to be browser-unsafe.
	 * 
	 * @param shorthandName
	 *            the name of the shorthand property.
	 * @param valueText
	 *            the unsafe value text.
	 */
	void shorthandWarning(String shorthandName, String valueText);

	/**
	 * Generic error while processing a shorthand.
	 * 
	 * @param shorthandName
	 *            the shorthand name.
	 * @param message
	 *            error message.
	 */
	void shorthandError(String shorthandName, String message);

	/**
	 * When processing a shorthand, some values could not be assigned.
	 * 
	 * @param shorthandName
	 *            the name of the shorthand property being processed.
	 * @param unassignedProperties
	 *            a string array with the names of the subproperties that went
	 *            without an assigned value and were reset to default values.
	 * @param unassignedValues
	 *            the array of unassigned lexical unit values.
	 */
	void unassignedShorthandValues(String shorthandName, String[] unassignedProperties, LexicalUnit[] unassignedValues);

	/**
	 * When processing a shorthand, an individual value could not be properly
	 * assigned to any subproperty.
	 * 
	 * @param shorthandName
	 *            the name of the shorthand property being processed.
	 * @param valueCss
	 *            the css text of the unassigned value.
	 */
	void unassignedShorthandValue(String shorthandName, String valueCss);

	/**
	 * The number of subproperty values found in a shorthand is wrong.
	 * 
	 * @param shorthandName
	 *            the name of the shorthand property being processed.
	 * @param count
	 *            the number of values found.
	 */
	void wrongSubpropertyCount(String shorthandName, int count);

	/**
	 * An unrecognized CSS identifier value was found for property
	 * <code>propertyName</code>.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param ident
	 *            the unrecognized identifier.
	 */
	void unknownIdentifier(String propertyName, String ident);

	/**
	 * A required property was missing when processing a shorthand.
	 * 
	 * @param propertyName
	 *            the missing property name.
	 */
	void missingRequiredProperty(String propertyName);

	/**
	 * The property <code>propertyName</code> has a wrong value.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param e
	 *            the exception describing the problem.
	 */
	void wrongValue(String propertyName, CSSPropertyValueException e);

	/**
	 * A syntax issue was found, but not serious enough to trigger an error.
	 * 
	 * @param message a message describing the problem.
	 */
	void syntaxWarning(String message);

	/**
	 * A warning due to the processing of IE-compatible values was issued.
	 * 
	 * @param propertyName
	 *            the property name.
	 * @param cssText
	 *            the faulty cssText.
	 */
	void compatWarning(String propertyName, String cssText);

	/**
	 * Unable to find containing block for <code>containedNode</code>.
	 *
	 * @param containedNode
	 *            the contained node.
	 * @param ownerNode
	 *            the owner node.
	 */
	void noContainingBlock(String containedNode, Node ownerNode);

	/**
	 * Reports a SAC warning as per the SAC ErrorHandler.
	 * 
	 * @param exception
	 *            the parse exception.
	 * @param previousIndex
	 *            the index for the previously set property in the properties
	 *            collection, or -1 if the problem occurred with the first
	 *            property or before that.
	 */
	void sacWarning(CSSParseException exception, int previousIndex);

	/**
	 * Reports a SAC error as per the SAC ErrorHandler.
	 * 
	 * @param exception
	 *            the parse exception.
	 * @param previousIndex
	 *            the index for the previously set property in the properties
	 *            collection, or -1 if the problem occurred with the first
	 *            property or before that.
	 */
	void sacError(CSSParseException exception, int previousIndex);

	/**
	 * Have errors been reported to this error handler?
	 * 
	 * @return <code>true</code> if errors were reported, <code>false</code> otherwise.
	 */
	boolean hasErrors();

	/**
	 * Have value warnings been reported to this error handler?
	 *
	 * @return <code>true</code> if value warnings were reported, <code>false</code> otherwise.
	 */
	boolean hasWarnings();

	/**
	 * Reset this handler and prepare it to handle a new declaration.
	 */
	void reset();

}
