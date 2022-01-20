/*

 Copyright (c) 2005-2022, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.dom;

import org.w3c.dom.TypeInfo;

abstract class DOMTypeInfo implements TypeInfo {

	@Override
	public String getTypeName() {
		return null;
	}

	@Override
	public boolean isDerivedFrom(String typeNamespaceArg, String typeNameArg, int derivationMethod) {
		return false;
	}

}
