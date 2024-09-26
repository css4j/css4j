/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.util.EnumSet;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSStyleSheetFactory;
import io.sf.carte.doc.style.css.DocumentCSSStyleSheet;
import io.sf.carte.doc.style.css.MediaQueryList;
import io.sf.carte.doc.style.css.nsac.Parser;
import io.sf.carte.doc.style.css.property.TypedValue;
import io.sf.carte.doc.style.css.property.ValueFactory;

/**
 * Abstract class for CSS style sheet factories.
 * 
 * @author Carlos Amengual
 */
abstract public class AbstractCSSStyleSheetFactory implements CSSStyleSheetFactory, java.io.Serializable {

	private static final long serialVersionUID = 2L;

	/**
	 * Create an inline style.
	 * 
	 * @param owner
	 *            the style's owner node (an attribute node).
	 * 
	 * @return the inline style.
	 */
	abstract protected InlineStyle createInlineStyle(Node owner);

	@Override
	abstract public AbstractCSSStyleSheet createStyleSheet(String title, MediaQueryList media);

	/**
	 * Creates a CSS style sheet owned by a CSS rule.
	 * <p>
	 * 
	 * @param ownerRule
	 *            the owner rule.
	 * @param title
	 *            the advisory title.
	 * @param mediaList
	 *            the list of target media for style.
	 * @return the style sheet.
	 */
	abstract protected AbstractCSSStyleSheet createRuleStyleSheet(AbstractCSSRule ownerRule, String title,
			MediaQueryList mediaList);

	/**
	 * Creates an author (document-linked) CSS style sheet.
	 * <p>
	 * 
	 * @param ownerNode
	 *            the node that associates the style sheet to the document. In HTML it can be
	 *            a <code>link</code> or <code>style</code> element. For style sheets that are
	 *            included by other style sheets, the value of this parameter is
	 *            <code>null</code>.
	 * @param title
	 *            the advisory title.
	 * @param mediaList
	 *            the target media list for style.
	 * @return the style sheet.
	 */
	abstract protected AbstractCSSStyleSheet createLinkedStyleSheet(Node ownerNode, String title,
			MediaQueryList mediaList);

	@Override
	abstract public AbstractCSSStyleDeclaration createAnonymousStyleDeclaration(Node node);

	@Override
	abstract public TypedValue getSystemDefaultValue(String propertyName);

	/**
	 * Create a NSAC Parser with the proper flags enabled.
	 * 
	 * @return the NSAC parser.
	 */
	abstract protected Parser createSACParser();

	/**
	 * Get the parser flags that should be used by NSAC parsers.
	 * 
	 * @return the NSAC parser flags.
	 */
	abstract protected EnumSet<Parser.Flag> getParserFlags();

	/**
	 * Check for compat value flags.
	 * 
	 * @return <code>true</code> if the factory has compat value flags set.
	 */
	abstract protected boolean hasCompatValueFlags();

	/**
	 * Check whether the given factory flag is set.
	 * 
	 * @param flag the flag.
	 * @return {@code true} if the flag is set.
	 */
	abstract protected boolean hasFactoryFlag(short flag);

	/**
	 * Get a value factory set to the appropriate flags.
	 * 
	 * @return the value factory.
	 */
	abstract protected ValueFactory getValueFactory();

	/**
	 * Get the priority-important part of the user style sheet, <i>i.e.</i> the style sheet
	 * with the declarations of <code>important</code> priority.
	 * 
	 * @return the important part of the user style sheet.
	 */
	abstract protected AbstractCSSStyleSheet getUserImportantStyleSheet();

	/**
	 * Get the normal part of the user style sheet, <i>i.e.</i> the style sheet with the
	 * declarations of normal priority.
	 * 
	 * @return the normal part of the user style sheet.
	 */
	abstract protected AbstractCSSStyleSheet getUserNormalStyleSheet();

	/**
	 * Sets a default HTML default style sheet as the user agent style sheet.
	 * <p>
	 * The sheet will be appropriately merged with the non-important part of the
	 * user-preference style sheet to provide the document's default sheet.
	 * </p>
	 */
	@Override
	abstract public void setDefaultHTMLUserAgentSheet();

	/**
	 * Get the style sheet resulting from the merge of the user agent sheet and the
	 * non-important part of the user style sheet.
	 * 
	 * @param mode
	 *            the compliance mode.
	 * @return the default style sheet at the top of the cascade.
	 */
	abstract protected DocumentCSSStyleSheet getDefaultStyleSheet(CSSDocument.ComplianceMode mode);

}
