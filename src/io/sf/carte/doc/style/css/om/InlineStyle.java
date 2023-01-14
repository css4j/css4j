/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.io.IOException;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSElement;
import io.sf.carte.doc.style.css.CSSTypedValue;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.NodeStyleDeclaration;
import io.sf.carte.doc.style.css.StyleDeclarationErrorHandler;
import io.sf.carte.doc.style.css.StyleFormattingContext;
import io.sf.carte.doc.style.css.nsac.Condition;
import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.util.SimpleWriter;

/**
 * CSS Inline style declaration.
 * 
 */
abstract public class InlineStyle extends BaseCSSStyleDeclaration implements NodeStyleDeclaration {

	private static final long serialVersionUID = 1L;

	private Node node = null;

	protected InlineStyle() {
		super();
	}

	protected InlineStyle(InlineStyle copiedObject) {
		super(copiedObject);
		setOwnerNode(copiedObject.getOwnerNode());
	}

	@Override
	protected void writeShorthandCssText(SimpleWriter wri, StyleFormattingContext context, String shorthandName,
			ShorthandValue shval) throws IOException {
		wri.write(shorthandName);
		context.writeColon(wri);
		context.writeShorthandValue(wri, shorthandName, shval);
		if (shval.isImportant()) {
			context.writeImportantPriority(wri);
		}
		context.writeSemiColon(wri);
		context.endInlinePropertyDeclaration(wri);
	}

	@Override
	protected void writeLonghandCssText(SimpleWriter wri, StyleFormattingContext context, String ptyname,
			StyleValue ptyvalue, boolean important) throws IOException {
		wri.write(ptyname);
		context.writeColon(wri);
		writeValue(wri, ptyname, ptyvalue);
		if (important) {
			context.writeImportantPriority(wri);
		}
		context.writeSemiColon(wri);
		context.endInlinePropertyDeclaration(wri);
	}

	private void writeValue(SimpleWriter wri, String propertyName, StyleValue value) throws IOException {
		if (value.getCssValueType() != CssType.TYPED || value.getPrimitiveType() != CSSValue.Type.STRING) {
			value.writeCssText(wri);
		} else {
			CSSTypedValue primi = (CSSTypedValue) value;
			String s = primi.getStringValue();
			s = ParseHelper.escapeControl(s);
			s = ParseHelper.quote(s, '\'');
			wri.write(s);
		}
	}

	@Override
	public Node getOwnerNode() {
		return node;
	}

	protected void setOwnerNode(Node node) {
		this.node = node;
	}

	/**
	 * Has this style's owner element an override style attached to the given pseudo-element?
	 * 
	 * @param pseudoElt
	 *            the pseudo-element condition, or <code>null</code> if none.
	 * @return <code>true</code> if this style's owner element has an override style attached, false
	 *         otherwise.
	 */
	public boolean hasOverrideStyle(Condition pseudoElt) {
		Node node = getOwnerNode();
		if (node != null) {
			short type = node.getNodeType();
			if (type == Node.ATTRIBUTE_NODE) {
				node = ((Attr) node).getOwnerElement();
				if (node == null) {
					return false;
				} else {
					type = node.getNodeType();
				}
			}
			if (type == Node.ELEMENT_NODE) {
				return ((CSSElement) node).hasOverrideStyle(pseudoElt);
			}
		}
		return false;
	}

	@Override
	public StyleDeclarationErrorHandler getStyleDeclarationErrorHandler() {
		Node node = getOwnerNode();
		if (node != null) {
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				CSSElement owner = (CSSElement) ((Attr) node).getOwnerElement();
				if (owner != null) {
					return owner.getOwnerDocument().getErrorHandler().getInlineStyleErrorHandler(owner);
				}
			}
		}
		return null;
	}

	@Override
	abstract public InlineStyle clone();

}
