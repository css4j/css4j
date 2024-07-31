/*

 Copyright (c) 1998-2024, Carlos Amengual.

 SPDX-License-Identifier: BSD-3-Clause

 Licensed under a BSD-style License. You can find the license here:
 https://css4j.github.io/LICENSE.txt

 */

package io.sf.carte.doc;

/**
 * Generic document-related exception.
 * <p>
 * Generally contains domain-specific exceptions that give more information,
 * like <code>SAXException</code>s or implementation-specific exceptions.
 * 
 * @author Carlos Amengual
 * @version 1.01
 */
public class DocumentException extends Exception {

	private static final long serialVersionUID = 2L;

	public DocumentException() {
		super();
	}

	public DocumentException(String message) {
		super(message);
	}

	public DocumentException(String message, Throwable cause) {
		super(message, cause);
	}

	public DocumentException(Throwable cause) {
		super(cause);
	}

}