/*

 Copyright (c) 2005-2020, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Node;

import io.sf.carte.doc.style.css.CSSDocument;
import io.sf.carte.doc.style.css.CSSValue;
import io.sf.carte.doc.style.css.CSSValue.CssType;
import io.sf.carte.doc.style.css.property.StyleValue;
import io.sf.carte.doc.style.css.property.URIValue;
import io.sf.carte.doc.style.css.property.URIValueWrapper;
import io.sf.carte.doc.style.css.property.ValueList;

/**
 * Wrapped CSS Style Declaration.
 * 
 */
class WrappedCSSStyleDeclaration extends BaseCSSStyleDeclaration {

	private final String hrefcontext;
	private final String oldHrefContext;

	/**
	 * Constructor with parent CSS rule argument.
	 * 
	 * @param parentRule
	 *            the parent CSS rule.
	 */
	protected WrappedCSSStyleDeclaration(BaseCSSDeclarationRule parentRule) {
		super(parentRule);
		hrefcontext = getHrefContext(parentRule);
		this.oldHrefContext = hrefcontext;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param copiedObject
	 *            the BaseCSSStyleDeclaration to be copied.
	 */
	protected WrappedCSSStyleDeclaration(BaseCSSStyleDeclaration copiedObject, String oldHrefContext) {
		super(copiedObject);
		hrefcontext = getHrefContext(getParentRule());
		this.oldHrefContext = oldHrefContext;
	}

	static String getHrefContext(BaseCSSDeclarationRule parentRule) {
		String hrefcontext = null;
		AbstractCSSStyleSheet parentSheet = parentRule.getParentStyleSheet();
		Node node = parentSheet.getOwnerNode();
		String parentHref = parentSheet.getHref();
		if (parentHref != null) {
			if (!parentHref.contains("://")) {
				// Relative URI, try to find absolute
				if (node != null) {
					try {
						hrefcontext = new URL(((CSSDocument) node.getOwnerDocument()).getBaseURL(), parentHref)
								.toExternalForm();
					} catch (MalformedURLException e) {
						parentRule.getStyleDeclarationErrorHandler().malformedURIValue(parentHref);
					}
				}
			} else {
				hrefcontext = parentHref;
			}
		} else {
			if (node != null) {
				URL baseurl = ((CSSDocument) node.getOwnerDocument()).getBaseURL();
				if (baseurl != null) {
					hrefcontext = baseurl.toExternalForm();
				}
			}
		}
		return hrefcontext;
	}

	@Override
	protected StyleValue getCSSValue(String propertyName) {
		StyleValue value = super.getCSSValue(propertyName);
		if (value != null) {
			value = wrapCSSValue(value, oldHrefContext, hrefcontext);
		}
		return value;
	}

	static StyleValue wrapCSSValue(StyleValue value, String oldHrefContext, String hrefcontext) {
		CssType type = value.getCssValueType();
		if (type == CssType.LIST) {
			if (hrefcontext != null) {
				value = ((ValueList) value).wrap(oldHrefContext, hrefcontext);
			}
		} else if (value.getPrimitiveType() == CSSValue.Type.URI) {
			if (hrefcontext != null) {
				value = new URIValueWrapper((URIValue) value, oldHrefContext, hrefcontext);
			}
		}
		return value;
	}

	@Override
	public BaseCSSStyleDeclaration clone() {
		return new WrappedCSSStyleDeclaration(getParentRule());
	}

}
