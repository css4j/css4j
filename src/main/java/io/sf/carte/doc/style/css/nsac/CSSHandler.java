/*
 * This software includes material derived from SAC (https://www.w3.org/TR/SAC/).
 * Copyright © 1999,2000 W3C® (MIT, INRIA, Keio). All Rights Reserved.
 * https://www.w3.org/Consortium/Legal/copyright-software-19980720
 *
 * The original version of this interface comes from SAX :
 * http://www.megginson.com/SAX/
 *
 * Copyright © 2017-2025, Carlos Amengual.
 *
 * SPDX-License-Identifier: W3C-19980720
 *
 */
package io.sf.carte.doc.style.css.nsac;

import io.sf.carte.doc.style.css.BooleanCondition;
import io.sf.carte.doc.style.css.CSSStyleSheet;
import io.sf.carte.doc.style.css.MediaQueryList;

/**
 * A CSS event handler for low-level parsing.
 * <p>
 * Based on SAC's {@code DocumentHandler} interface by Philippe Le Hegaret.
 * </p>
 */
public interface CSSHandler {

	/**
	 * Receive notification of the beginning of the parse process.
	 * <p>
	 * The CSS parser will invoke this method only once, before any other methods in
	 * this interface, and only when one of the {@code parseStyleSheet},
	 * {@code parseRule} or {@code parseStyleDeclaration} methods were called.
	 * </p>
	 * 
	 * @param parserctl an object that allows convenient access to certain parser
	 *                  functionalities.
	 */
	void parseStart(ParserControl parserctl);

	/**
	 * Receive notification of the end of the stream being parsed.
	 * <p>
	 * The CSS parser will invoke this method only once, and it will be the last
	 * method invoked during the parse. The parser shall not invoke this method
	 * unless it has reached the end of input.
	 * </p>
	 */
	void endOfStream();

	/**
	 * Receive notification of a comment. If the comment appears in a declaration
	 * (e.g. color: /* comment {@literal *}/ blue;), the parser notifies the comment
	 * before the declaration.
	 * 
	 * @param text         The comment.
	 * @param precededByLF {@code true} if a Line Feed character was found since the
	 *                     previous event.
	 */
	void comment(String text, boolean precededByLF);

	/**
	 * A {@code @charset} rule.
	 * 
	 * @param charset the character set.
	 */
	default void charset(String charset) {
	}

	/**
	 * Receive notification of an unknown {@literal @}-rule not supported by this parser.
	 *
	 * @param atRule The complete {@literal @}-rule.
	 */
	void ignorableAtRule(String atRule);

	/**
	 * Receive notification of a namespace declaration.
	 *
	 * @param prefix the namespace prefix, or the empty string if this is the
	 *               default namespace.
	 * @param uri    the URI for this namespace.
	 */
	void namespaceDeclaration(String prefix, String uri);

	/**
	 * Receive notification of a import rule in the style sheet.
	 *
	 * @param uri                 The URI of the imported style sheet.
	 * @param layerName           the layer name declared in the at-rule itself, or
	 *                            an empty string if the layer is anonymous, or
	 *                            {@code null} if the at-rule does not declare a
	 *                            layer.
	 * @param supportsCondition   the supports condition, or {@code null} if none.
	 * @param media               The intended destination media for style
	 *                            information.
	 * @param defaultNamespaceURI The default namespace URI for the imported style
	 *                            sheet.
	 */
	void importStyle(String uri, String layerName, BooleanCondition supportsCondition,
			MediaQueryList media, String defaultNamespaceURI);

	/**
	 * Receive notification of the beginning of a media rule.
	 * <p>
	 * The Parser will invoke this method at the beginning of every media rule in
	 * the style sheet. There will be a corresponding {@code endMedia()} event for
	 * every {@code startMedia()} event.
	 * </p>
	 *
	 * @param media The intended destination media for style information.
	 */
	void startMedia(MediaQueryList media);

	/**
	 * Receive notification of the end of a media rule.
	 *
	 * @param media The intended destination media for style information.
	 */
	void endMedia(MediaQueryList media);

	/**
	 * Receive notification of the beginning of a page rule.
	 * <p>
	 * The Parser will invoke this method at the beginning of every page rule in the
	 * style sheet. There will be a corresponding {@code endPage()} event for every
	 * {@code startPage()} event.
	 * </p>
	 *
	 * @param pageSelectorList the page selector list (if any, <code>null</code>
	 *                         otherwise)
	 */
	void startPage(PageSelectorList pageSelectorList);

	/**
	 * Receive notification of the end of a page rule.
	 * 
	 * @param pageSelectorList the page selector list (if any, <code>null</code>
	 *                         otherwise)
	 */
	void endPage(PageSelectorList pageSelectorList);

	/**
	 * Receive notification of the beginning of a margin-box rule.
	 * <p>
	 * The Parser will invoke this method at the beginning of every margin rule in
	 * the style sheet. There will be a corresponding {@code endMargin()} event for
	 * every {@code startMargin()} event.
	 * </p>
	 *
	 * @param name the name of the rule.
	 */
	void startMargin(String name);

	/**
	 * Receive notification of the end of a margin rule.
	 */
	void endMargin();

	/**
	 * Receive notification of the beginning of a font face rule.
	 * <p>
	 * The Parser will invoke this method at the beginning of every font face
	 * statement in the style sheet. There will be a corresponding
	 * {@code endFontFace()} event for every {@code startFontFace()} event.
	 * </p>
	 */
	void startFontFace();

	/**
	 * Receive notification of the end of a font face rule.
	 */
	void endFontFace();

	/**
	 * Start a {@literal @}counter-style rule.
	 * 
	 * @param name the counter-style name.
	 */
	void startCounterStyle(String name);

	/**
	 * End of {@literal @}counter-style rule.
	 */
	void endCounterStyle();

	/**
	 * Start a {@literal @}keyframes rule.
	 * 
	 * @param name the keyframes name.
	 */
	void startKeyframes(String name);

	/**
	 * End of {@literal @}keyframes rule.
	 */
	void endKeyframes();

	/**
	 * Start a {@literal @}keyframe.
	 * 
	 * @param keyframeSelector the keyframe selector.
	 */
	void startKeyframe(LexicalUnit keyframeSelector);

	/**
	 * End of {@literal @}keyframe.
	 */
	void endKeyframe();

	/**
	 * Start a font feature values rule.
	 * 
	 * @param familyName the font family names.
	 */
	void startFontFeatures(String[] familyName);

	/**
	 * End of font feature values rule.
	 */
	void endFontFeatures();

	/**
	 * Start a feature map.
	 * 
	 * @param mapName the map name.
	 */
	void startFeatureMap(String mapName);

	/**
	 * End of feature map.
	 */
	void endFeatureMap();

	/**
	 * Start a {@literal @}property rule.
	 * 
	 * @param name the custom property name.
	 */
	void startProperty(String name);

	/**
	 * End of {@literal @}property rule.
	 * 
	 * @param discard if {@code true}, the rule should be discarded.
	 */
	void endProperty(boolean discard);

	/**
	 * Receive notification of the beginning of a supports rule.
	 * <p>
	 * The Parser will invoke this method at the beginning of every supports rule in
	 * the style sheet. There will be a corresponding {@code endSupports()} event
	 * for every {@code startSupports()} event.
	 * </p>
	 *
	 * @param condition the supports condition.
	 */
	void startSupports(BooleanCondition condition);

	/**
	 * Receive notification of the end of a supports rule.
	 *
	 * @param condition the supports condition.
	 */
	void endSupports(BooleanCondition condition);

	/**
	 * Receive notification of the beginning of a style rule.
	 *
	 * @param selectors the intended selectors for next declarations.
	 */
	void startSelector(SelectorList selectors);

	/**
	 * Receive notification of the end of a style rule.
	 *
	 * @param selectors the intended selectors for the previous declarations.
	 */
	void endSelector(SelectorList selectors);

	/**
	 * Start a {@literal @}viewport rule.
	 * <p>
	 * Note: {@code @viewport} rules were
	 * <a href="https://github.com/w3c/csswg-drafts/issues/4766">removed by W3C in
	 * February 2020</a>.
	 * </p>
	 */
	default void startViewport() {
	}

	/**
	 * End of {@literal @}viewport rule.
	 */
	default void endViewport() {
	}

	/**
	 * Receive notification of a property declaration.
	 * 
	 * @param name      the name of the property.
	 * @param value     the value of the property.
	 * @param important is this property important ?
	 */
	void property(String name, LexicalUnit value, boolean important);

	/**
	 * Receive notification of a property declaration that must be processed as a
	 * lexical value.
	 * 
	 * @param name      the name of the property.
	 * @param value     the value of the property.
	 * @param important is this property important ?
	 */
	void lexicalProperty(String name, LexicalUnit value, boolean important);

	/**
	 * Obtain the style sheet.
	 * 
	 * @return a reference to the style sheet, or {@code null} if it couldn't be
	 *         obtained.
	 */
	default CSSStyleSheet<?> getStyleSheet() {
		return null;
	}

}
