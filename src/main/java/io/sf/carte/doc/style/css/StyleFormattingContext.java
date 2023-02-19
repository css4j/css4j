/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css;

import java.io.IOException;
import java.util.List;

import org.w3c.dom.css.CSSRule;

import io.sf.carte.util.SimpleWriter;

/**
 * Define methods for style text formatting.
 * <p>
 * Different implementations of this interface can provide different style text
 * formatting.
 * </p>
 * <p>
 * If the rule does not contain a style declaration (of either properties or
 * descriptors), the methods in this interface are not called.
 * </p>
 * 
 * @see StyleFormattingFactory
 * @see CSSStyleSheetFactory#setStyleFormattingFactory(StyleFormattingFactory)
 */
public interface StyleFormattingContext extends DeclarationFormattingContext {

	/**
	 * This auxiliary method can be used to deepen the indentation at places where
	 * the general serialization flow does not give the desired results.
	 * <p>
	 * It is used only by the {@code @font-feature-values} rule.
	 * </p>
	 */
	void deepenCurrentContext();

	/**
	 * Notify this context that the style was serialized for the given rule.
	 * <p>
	 * It is called before {@link #endStyleDeclaration(SimpleWriter)}.
	 * </p>
	 * 
	 * @param rule the rule being serialized.
	 */
	void endCurrentContext(CSSRule rule);

	/**
	 * End an inline property declaration.
	 * <p>
	 * Called after
	 * {@link DeclarationFormattingContext#writeSemiColon(SimpleWriter)}, only from
	 * inline styles.
	 * </p>
	 * 
	 * @param wri the writer to write to.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void endInlinePropertyDeclaration(SimpleWriter wri) throws IOException;

	/**
	 * Ends the serialization of a rule.
	 * <p>
	 * It is called after {@link #writeRightCurlyBracket(SimpleWriter)}.
	 * </p>
	 * 
	 * @param wri              the writer to write to.
	 * @param trailingComments the comments that go after the rule.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void endRule(SimpleWriter wri, List<String> trailingComments) throws IOException;

	/**
	 * Called after a list of rules is serialized.
	 * <p>
	 * May be used for indentation.
	 * </p>
	 * 
	 * @param wri the writer to write to.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void endRuleList(SimpleWriter wri) throws IOException;

	/**
	 * End a style declaration.
	 * <p>
	 * Called after {@link #endCurrentContext(CSSRule)} and before
	 * {@link #writeRightCurlyBracket(SimpleWriter)}.
	 * </p>
	 * 
	 * @param wri the writer to write to.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void endStyleDeclaration(SimpleWriter wri) throws IOException;

	/**
	 * Sets the parent of the rule being serialized, if any, providing additional
	 * context for the serialization.
	 * <p>
	 * It is called before {@link #startRule(SimpleWriter, List)}. However, this
	 * method being called does not guarantee that {@code startRule()} will be
	 * called.
	 * </p>
	 * 
	 * @param rule the parent rule. May be {@code null}.
	 */
	void setParentContext(CSSRule rule);

	/**
	 * Starts the serialization of a rule.
	 * <p>
	 * It is the first stage of the serialization of a rule. For example, in the
	 * case of style rules it is called before serializing the rule selector list.
	 * At-rules call it before writing the {@literal @}-rule name.
	 * </p>
	 * 
	 * @param wri               the writer to write to.
	 * @param precedingComments the comments that precede the rule.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void startRule(SimpleWriter wri, List<String> precedingComments) throws IOException;

	/**
	 * Start a style declaration (of either properties or descriptors).
	 * <p>
	 * Called after {@link #writeLeftCurlyBracket(SimpleWriter)} and before
	 * serializing the style declaration.
	 * </p>
	 * 
	 * @param wri the writer to write to.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void startStyleDeclaration(SimpleWriter wri) throws IOException;

	/**
	 * Update this context for the given rule.
	 * <p>
	 * For style rules, it is called after serializing the selector list and before
	 * {@link #writeLeftCurlyBracket(SimpleWriter)}. At-rules call it after writing
	 * the rule name.
	 * </p>
	 * <p>
	 * May be used for indentation.
	 * </p>
	 * 
	 * @param rule the rule being serialized.
	 */
	void updateContext(CSSRule rule);

	/**
	 * Serialize a comment that precedes a rule, with the appropriate indent.
	 * <p>
	 * Trailing comments are generally written directly by
	 * {@link #endRule(SimpleWriter, List)} because no indent is necessary.
	 * </p>
	 * 
	 * @param wri     the writer to write to.
	 * @param comment the comment to serialize.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeComment(SimpleWriter wri, String comment) throws IOException;

	/**
	 * Serialize an {@code important} priority declaration to a
	 * {@code SimpleWriter}.
	 * <p>
	 * If the property declaration has {@code important} priority, it is called
	 * after the value is written and before {@link #writeSemiColon(SimpleWriter)}.
	 * </p>
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeImportantPriority(SimpleWriter wri) throws IOException;

	/**
	 * Serialize a left curly bracket to a {@code SimpleWriter}.
	 * <p>
	 * Called after {@link #updateContext(CSSRule)} and before
	 * {@link #startStyleDeclaration(SimpleWriter)}.
	 * </p>
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeLeftCurlyBracket(SimpleWriter wri) throws IOException;

	/**
	 * Serialize one level of indentation to a {@code SimpleWriter}.
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeLevelIndent(SimpleWriter wri) throws IOException;

	/**
	 * Serialize a right curly bracket to a {@code SimpleWriter}.
	 * <p>
	 * Called after {@link #endStyleDeclaration(SimpleWriter)} and before
	 * {@link #endRule(SimpleWriter, List)}.
	 * </p>
	 * 
	 * @param wri the writer.
	 * @throws IOException if an I/O problem happens while writing to the
	 *                     {@code SimpleWriter}.
	 */
	void writeRightCurlyBracket(SimpleWriter wri) throws IOException;

	/**
	 * Serialize a css shorthand value to the given writer.
	 * <p>
	 * Although the <code>CSSShorthandValue</code> type contains priority
	 * information, this method only writes the value.
	 * </p>
	 * <p>
	 * The default implementation just writes {@link CSSValue#getCssText()}.
	 * </p>
	 * 
	 * @param wri           the writer.
	 * @param shorthandName the name of the shorthand property whose value is being
	 *                      printed.
	 * @param value         the value to write.
	 * @throws IOException if an error happened while writing.
	 */
	default void writeShorthandValue(SimpleWriter wri, String shorthandName,
			CSSShorthandValue value) throws IOException {
		wri.write(value.getCssText());
	}

}
