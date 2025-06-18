/*

 Copyright (c) 2005-2025, Carlos Amengual.

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

// SPDX-License-Identifier: BSD-3-Clause

package io.sf.carte.doc;

import org.w3c.dom.DOMException;

/**
 * An object doesn't belong somewhere.
 */
public class DOMHierarchyRequestException extends DOMException {

	private static final long serialVersionUID = 1L;

	public DOMHierarchyRequestException() {
		this(null);
	}

	public DOMHierarchyRequestException(String message) {
		super(DOMException.HIERARCHY_REQUEST_ERR, message);
	}

	public DOMHierarchyRequestException(String message, Throwable cause) {
		super(DOMException.HIERARCHY_REQUEST_ERR, message);
		initCause(cause);
	}

}
