package io.sf.carte.doc.style.css.util;

import java.util.HashMap;
import java.util.Map.Entry;

import io.sf.carte.doc.style.css.nsac.SheetContext;

class TestSheetContext implements SheetContext {

	private HashMap<String, String> mapNsPrefix2Uri = new HashMap<>();

	/*
	 * NamespaceMap
	 */

	@Override
	public String getNamespaceURI(String nsPrefix) {
		return mapNsPrefix2Uri.get(nsPrefix);
	}

	/*
	 * NamespacePrefixMap
	 */

	@Override
	public String getNamespacePrefix(String namespaceUri) {
		for (Entry<String, String> me : mapNsPrefix2Uri.entrySet()) {
			if (namespaceUri.equals(me.getValue())) {
				return me.getKey();
			}
		}
		return null;
	}

	@Override
	public boolean hasDefaultNamespace() {
		return mapNsPrefix2Uri.containsKey("");
	}

	@Override
	public void registerNamespacePrefix(String prefix, String uri) {
		mapNsPrefix2Uri.put(prefix, uri);
	}

	@Override
	public boolean hasFactoryFlag(short flag) {
		return false;
	}

}
