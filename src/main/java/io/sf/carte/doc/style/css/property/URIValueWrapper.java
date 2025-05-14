/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc.style.css.property;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.w3c.dom.DOMException;

import io.sf.carte.doc.style.css.parser.ParseHelper;
import io.sf.carte.util.BufferSimpleWriter;
import io.sf.carte.util.SimpleWriter;

/**
 * This class is mostly for internal use by the library (cross-package, so its
 * visibility is public).
 */
public class URIValueWrapper extends URIValue implements WrappedValue {

	private static final long serialVersionUID = 1L;

	private final String parentSheetHref;

	public URIValueWrapper(URIValue wrapped, String oldHrefContext, String parentSheetHref) {
		super(wrapped);
		this.parentSheetHref = parentSheetHref;
		if (oldHrefContext != parentSheetHref) {
			setStringValue(getStringValue(super.getStringValue(), oldHrefContext));
		}
	}

	@Override
	public String getParentSheetHref() {
		return parentSheetHref;
	}

	@Override
	public String getCssText() {
		String sv = getStringValue();
		if (sv == null) {
			return "";
		}
		BufferSimpleWriter sw = new BufferSimpleWriter(sv.length() + 6);
		try {
			writeCssText(sw);
		} catch (IOException e) {
		}
		return sw.toString();
	}

	@Override
	public void writeCssText(SimpleWriter wri) throws IOException {
		String sv = getStringValue();
		if (sv != null) {
			wri.write("url(");
			sv = ParseHelper.quote(sv, quote);
			wri.write(sv);
			wri.write(')');
		}
	}

	@Override
	public String getMinifiedCssText(String propertyName) {
		String sv = getStringValue();
		if (sv == null) {
			return "";
		}
		int lastSlash;
		if (parentSheetHref == null || (lastSlash = parentSheetHref.lastIndexOf('/')) == -1) {
			return super.getCssText();
		}
		String base = parentSheetHref.substring(0, lastSlash + 1);
		URI uri, baseUri;
		try {
			uri = new URI(sv);
			baseUri = new URI(base);
		} catch (URISyntaxException e) {
			return super.getCssText();
		}
		baseUri = baseUri.normalize();
		String reluri = baseUri.relativize(uri).toASCIIString();
		String quoted = ParseHelper.quote(reluri, quote);
		return "url(" + quoted + ')';
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		error();
	}

	@Override
	public String getStringValue() {
		return getStringValue(super.getStringValue(), parentSheetHref);
	}

	private String getStringValue(String relValue, String contextHref) {
		if (relValue != null) {
			try {
				URI uri = new URI(relValue);
				if (!uri.isAbsolute() && contextHref != null) {
					URI base = new URI(contextHref);
					uri = base.resolve(uri);
				}
				relValue = uri.normalize().toASCIIString();
			} catch (Exception e) {
			}
		}
		return relValue;
	}

	@Override
	public void setStringValue(Type stringType, String stringValue) throws DOMException {
		error();
	}

	private void error() throws DOMException {
		throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
				"This property is read-only computed value. Must modify at the original style sheet.");
	}

	@Override
	public URL getURLValue() {
		URL url = null;
		String sv = super.getStringValue();
		if (sv != null) {
			try {
				URI uri = new URI(sv);
				if (!uri.isAbsolute()) {
					if (parentSheetHref != null) {
						URI base = new URI(parentSheetHref);
						uri = base.resolve(uri);
					} else {
						return null;
					}
				}
				url = uri.toURL();
			} catch (Exception e) {
			}
		}
		return url;
	}

	@Override
	public URIValue clone() {
		return new URIValueWrapper(super.clone(), this.parentSheetHref, this.parentSheetHref);
	}

}
