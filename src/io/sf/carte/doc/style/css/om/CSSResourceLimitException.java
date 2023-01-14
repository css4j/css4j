/*

 Copyright (c) 2005-2023, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc.style.css.om;

import org.w3c.dom.DOMException;

/**
 * A resource limit was found.
 */
class CSSResourceLimitException extends DOMException {

	private static final long serialVersionUID = 1L;

	CSSResourceLimitException(String message) {
		super(DOMException.INVALID_ACCESS_ERR, message);
	}

}
