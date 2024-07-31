/*

 Copyright (c) 2005-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;

import io.sf.carte.doc.StringList;
import io.sf.carte.doc.WrapperStringList;

class CSSDOMConfiguration implements DOMConfiguration, java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String,Object> configParameters;

	boolean cssWhitespaceProcessing = true;
	boolean normalizeCharacters = false;
	boolean keepComments = true;
	boolean useComputedStyles = false;

	CSSDOMConfiguration() {
		super();
		configParameters = new HashMap<>();
		configParameters.put("css-whitespace-processing", cssWhitespaceProcessing);
		configParameters.put("use-computed-styles", useComputedStyles);
		configParameters.put("comments", keepComments);
		configParameters.put("normalize-characters", normalizeCharacters);
	}

	@Override
	public boolean canSetParameter(String name, Object value) {
		if (name.equals("css-whitespace-processing") || name.equals("normalize-characters")
				|| name.equals("comments") || name.equals("use-computed-styles") || name.equals("well-formed")) {
			return value instanceof Boolean;
		} else if (value instanceof Boolean) {
			if (!((Boolean) value).booleanValue()) {
				return name.equals("canonical-form") || name.equals("check-character-normalization")
						|| name.equals("datatype-normalization") || name.equals("validate")
						|| name.equals("validate-if-schema");
			} else {
				return name.equals("element-content-whitespace") || name.equals("cdata-sections")
						|| name.equals("namespaces") || name.equals("namespace-declarations");
			}
		}
		return false;
	}

	@Override
	public Object getParameter(String name) throws DOMException {
		return configParameters.get(name);
	}

	@Override
	public StringList getParameterNames() {
		return new WrapperStringList(configParameters.keySet());
	}

	@Override
	public void setParameter(String name, Object value) throws DOMException {
		if (canSetParameter(name, value)) {
			if (name.equals("css-whitespace-processing")) {
				cssWhitespaceProcessing = (boolean) value;
			} else if (name.equals("normalize-characters")) {
				normalizeCharacters = (boolean) value;
			} else if (name.equals("comments")) {
				keepComments = (boolean) value;
			} else if (name.equals("use-computed-styles")) {
				useComputedStyles = (boolean) value;
			}
			configParameters.put(name, value);
		}
	}

}
